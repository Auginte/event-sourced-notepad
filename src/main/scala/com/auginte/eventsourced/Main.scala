package com.auginte.eventsourced

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.{model => jm}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.{Path => P}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, _}
import akka.stream.scaladsl.{Source, _}
import akka.stream.{ActorMaterializer, _}
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._
import scala.language.implicitConversions

object Main {

  def thread(execution: => Unit): Thread = new Thread(new Runnable {
    override def run(): Unit = execution
  })

  def html(data: String) = HttpResponse(StatusCodes.OK, List(), HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), data))

  def js(data: String) = HttpResponse(StatusCodes.OK, List(), HttpEntity(ContentType(MediaTypes.`application/javascript`, HttpCharsets.`UTF-8`), data))

  val ProjectName = Segment

  val StringUUID: PathMatcher1[UUID] =
    PathMatcher("""[\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12}""".r) flatMap { string ⇒
      try Some(string)
      catch {
        case _: IllegalArgumentException ⇒ None
      }
    }

  def env(param: String, default: String = ""): String = sys.props.getOrElse(param, default)

  // Storage example
  val storagePath = env("auginte.storage.path", "./data")
  val storage = new Storage(storagePath)

  def main(args: Array[String]) {

    val host = env("auginte.host", "127.0.0.1")
    val port = Integer.decode(env("auginte.port", "8112"))

    println(s"Starting service: http://$host:$port")

    implicit val system = ActorSystem("common-actor")
    implicit val materializer = ActorMaterializer()
    implicit val timeout = Timeout(5.seconds)
    implicit val executionContext = system.dispatcher
    val sessionFactory = SessionFactory(storage, system)

    val eventSourcedContentType = ContentType.parse("text/event-stream;charset=UTF-8").right.get
    val noCache = `Cache-Control`(CacheDirectives.`no-cache`)

    def showIndex = html(Html.index)

    def showProject(project: Project) = {
      val session = sessionFactory.newSession(project)
      html(Html.project(project, session.uuid))
    }

    case class SessionValidated(project: Project, uuid: UUID) {
      val session: SessionStream = sessionFactory.get(project, uuid) match {
        case Some(s) => s
        case None =>
          throw new IllegalArgumentException("No session was found by project and uuid. Go to project first")
      }
    }

    def storeAndExecute(rawData: Source[ByteString, _], session: SessionStream): HttpResponse = {
      def informOtherClients() = Flow.fromFunction[String, Boolean] { data =>
        sessionFactory.publish(data, session.project)
      }

      def logStorageErrors = Flow.fromFunction[(String, Boolean), String] { inputPair =>
        val (data, stored) = inputPair
        if (!stored) {
          println(s"Failed to store ${session.project}/${session.uuid}: $data")
        }
        data
      }

      def storeToDatabase: Flow[String, (String, Boolean), NotUsed] = Flow.fromFunction { data =>
        val success = storage.append(session.project, data)
        (data, success)
      }

      def processCommands = GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val input = b.add(Broadcast[String](2)) // Is blocking operation
        val output = b.add(Flow[(String)])

        input ~> storeToDatabase ~> logStorageErrors ~> informOtherClients ~> Sink.ignore
        input ~> output

        FlowShape(input.in, output.outlet)
      }

      val array = rawData
        .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
        .map(_.utf8String)
        .via(processCommands)
        .runReduce((a, b) => a + "," + b).map(i => "[" + i + "]").map(ByteString.apply)

      HttpResponse(
        StatusCodes.OK,
        List(),
        HttpEntity(ContentType(MediaTypes.`application/json`), Source.fromFuture(array))
      )
    }

    def toEventSourcedFormat(data: String): String = {
      def newUuid: String = java.util.UUID.randomUUID.toString

      s"""
         |id: $newUuid
         |data: $data
              """.stripMargin + "\n\n"
    }

    def streamSessionEvents(sessionStream: SessionStream) = {
      HttpResponse(
        StatusCodes.OK,
        headers = List(noCache),
        entity = HttpEntity.CloseDelimited(
          eventSourcedContentType,
          Source.fromGraph(sessionStream.stream)
            .map(toEventSourcedFormat)
            .map(ByteString.fromString)
        )
      )
    }

    val routes =
      pathSingleSlash {
        get {
          complete(showIndex)
        }
      } ~
        (path("project" / ProjectName) & get) { project =>
          complete(showProject(project))
        } ~
        path("project" / ProjectName / StringUUID).as(SessionValidated) { sessionData =>
          post { context =>
            context.complete {
              storeAndExecute(context.request.entity.dataBytes, sessionData.session)
            }
          } ~
            get {
              complete {
                streamSessionEvents(sessionData.session)
              }
            }
        } ~
        path("js" / "common.js") {
          complete {
            js(Html.commonJs)
          }
        }

    val bindingFuture = Http().bindAndHandle(routes, host, port)
  }
}


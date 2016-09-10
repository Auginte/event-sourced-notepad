package com.auginte.eventsourced

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.javadsl.{model => jm}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Source, _}
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._

object Main {

  def thread(execution: => Unit): Thread = new Thread(new Runnable {
    override def run(): Unit = execution
  })

  def newUuid: String = java.util.UUID.randomUUID.toString

    // Storage example
  val storagePath = env("auginte.storage.path", "./data")
  val storage = new Storage(storagePath)

  def env(param: String, default: String = ""): String = sys.props.getOrElse(param, default)

  def main(args: Array[String]) {

    val host = env("auginte.host", "127.0.0.1")
    val port = Integer.decode(env("auginte.port", "8112"))

    println(s"Starting service: http://$host:$port")

    implicit val system = ActorSystem("common-actor")
    implicit val materializer = ActorMaterializer()
    implicit val timeout = Timeout(5.seconds)
    implicit val executionContext = system.dispatcher

    val sendingMessages = thread{
      println("Starting")
      Thread.sleep(500)
      for (i <- 1 to 100) {
        println("Bla " + i)
        val json = s"""{"data":$i}"""
        RealTimeMessages.publishData(json)
        Thread.sleep(500)
      }
      println("Finished")
    }
    sendingMessages.start()

    def date = new java.util.Date().toString

    val linearFlow = Source.fromIterator[String](() => new Iterator[String] {
      private var i: Int = 1

      override def hasNext: Boolean = i < 50

      override def next(): String = {
        println("INITIAL STARTED : " + i + " | " + date)

        Thread.sleep(20)

        println("INITIAL FINISHED: " + i + " | " + date)
        i = i + 1

        s"""
           |id: $i
           |data: {"some":"data:$i"}
      """.stripMargin + "\n\n"
      }
    })

    val slowUpdate = Flow.fromFunction { b: ByteString =>
      //Doing some stuff
      println("Updating: Started")
      Thread.sleep(1)
      println("Updating: FINISHED")
      b
    }

    val customContentType =
      ContentType.parse("text/event-stream;charset=UTF-8").right.get
    val noCache =
      `Cache-Control`(CacheDirectives.`no-cache`)
    val allowOriginAll =
      `Access-Control-Allow-Origin`.*

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest].map {
      case r@HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>

        def toEventSourcedFormat(data: String): String = {
          s"""
             |id: $newUuid
             |data: $data
      """.stripMargin + "\n\n"
        }

        HttpResponse(
          StatusCodes.OK,
          headers = List(noCache, allowOriginAll),

          entity = HttpEntity.CloseDelimited(// <--- Response
            customContentType,
            RealTimeMessages.source()
              .map(toEventSourcedFormat)
              .map(ByteString.fromString) // <--- Stream
              .via(slowUpdate)
          )

        )

      case r@HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(Uri.Path("/project/")) =>
        val project = uri.path.reverse.head.toString

        HttpResponse(
          StatusCodes.OK,
          List(),
          HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), Html.page(project))
        )

      case r@HttpRequest(POST, uri, _, entity, _) if uri.path.startsWith(Uri.Path("/project/")) =>
        val project = uri.path.reverse.head.toString

        val array = entity.dataBytes
          .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
          .map(_.utf8String)
          .via(processCommands(project))
          .runReduce((a, b) => a + "," + b).map(i => "[" + i + "]").map(ByteString.apply)

        HttpResponse(
          StatusCodes.OK,
          List(),
          HttpEntity(ContentType(MediaTypes.`application/json`), Source.fromFuture(array))
        )

      case r@HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        println(r)

        HttpResponse(
          StatusCodes.OK,
          List(),
          HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), Html.consumer)
        )
      case r: HttpRequest =>
        println("Other request: " + r)
        HttpResponse(StatusCodes.NotFound)
    }

    val bindingFuture = Http().bindAndHandle(flow, host, port)
  }

  def processCommands(project: String) = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val input = b.add(Broadcast[String](3))
    val output = b.add(Flow[(String)].buffer(5, OverflowStrategy.backpressure))

    input ~> logEvent ~> Sink.ignore
    input ~> storeToDatabase(project) ~> Sink.ignore
    input ~> output

    FlowShape(input.in, output.outlet)
  }

  def logEvent: Flow[String, Unit, NotUsed] = Flow.fromFunction(data => println(s"Log: $data"))

  def storeToDatabase(project: String): Flow[String, Boolean, NotUsed] = Flow.fromFunction{ data =>
    storage.append(project, data)
  }
}


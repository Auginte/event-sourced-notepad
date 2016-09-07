package com.auginte.eventsourced

import akka.actor.ActorSystem
import akka.http.javadsl.{model => jm}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, _}
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._
import scala.util.Random

object Main {
  def env(param: String, default: String = ""): String = sys.props.getOrElse(param, default)

  def main(args: Array[String]) {

    val host = env("auginte.host", "127.0.0.1")
    val port = Integer.decode(env("auginte.port", "8112"))

    println(s"Starting service: http://$host:$port")

    implicit val system = ActorSystem("common-actor")
    implicit val materializer = ActorMaterializer()
    implicit val timeout = Timeout(5.seconds)

    // Storage example
    val storagePath = env("auginte.storage.path", "./data")
    val storage = new Storage(storagePath)
    val exampleCluster = "test"
    val success = storage.append(exampleCluster, System.nanoTime() + "DATA")
    println(success)
//    storage.readAll(exampleCluster).runForeach(println)

    def date = new java.util.Date().toString

    val linearFlow = Source.fromIterator[String](() => new Iterator[String] {
      private var i: Int = 1

      override def hasNext: Boolean = i < 5000

      override def next(): String = {
        println("INITIAL STARTED : " + i + " | " + date)

        Thread.sleep(2)

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
      case r @ HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
        println("Debug: Stream")

        println(r)

        HttpResponse(
          StatusCodes.OK,
          headers = List(noCache, allowOriginAll),

          entity = HttpEntity.CloseDelimited(// <--- Response
            customContentType,
            linearFlow.map(ByteString.fromString) // <--- Stream
              .via(slowUpdate)
          )

        )

      case r @ HttpRequest(GET, uri, _, _, _) if uri.path.startsWith(Uri.Path("/project/")) =>
        val project = uri.path.reverse.head.toString

        HttpResponse(
          StatusCodes.OK,
          List(),
          HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), Html.page(project))
        )

      case r @ HttpRequest(GET, Uri.Path("/"), _, _, _) =>
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
}


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpProtocols, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object PostMethod extends App{

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  val port = 9000

  // Definir una función de manejo de CORS
  def corsHandler(r: Route): Route = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST),
      `Access-Control-Allow-Headers`("Content-Type")
    ) {
      r
    }
  }


  val stringRoute : Route = {
    corsHandler{
      path("endpoint") {
        post {
          entity(as[String]) { requestBody =>
            complete {
              // Aquí puedes procesar el cuerpo de la solicitud POST
              // y generar la respuesta deseada
              val responseBody = s"Mesaje enviado -> $requestBody"
              val result : Future[String] = Future(requestBody)
              result.onComplete {
                case Success(result) => println(s"request body: $result")
                case Failure(exception) => println(s"error: $exception")
              }

              HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, responseBody))

            }
          }
        }
      }
    }
  }

  val jsonRoute : Route = {
    corsHandler{
      path("postCard") {
        entity(as[String]){ requestBody =>
          complete{
            val responseBody = s"post recibido -> $requestBody"
            val requestBodyFuture = Future(requestBody)
            requestBodyFuture.onComplete{
              case Success(result) => println(s"request body: $result")
              case Failure(exception) => println(s"error: $exception")
            }
            HttpResponse(StatusCodes.OK,Nil,entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,responseBody))
          }

        }
      }
    }
  }

  implicit val routes: Route = stringRoute ~ jsonRoute

  // Iniciar el servidor HTTP
  val bindingFuture = Http().newServerAt("localhost", port).bind(routes)
  println(s"Servidor iniciado en http://localhost:$port/")

  // Esperar hasta que se presione CTRL + C para terminar
  scala.io.StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}

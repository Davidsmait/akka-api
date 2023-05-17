import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods}
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object Main extends App {



  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route = path("hello") {
    get{
      respondWithHeaders(
        `Access-Control-Allow-Origin`.*,
        `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE, HttpMethods.OPTIONS),
        `Access-Control-Allow-Headers`("Content-Type", "Authorization")
      )
      {
        complete(HttpEntity(ContentTypes.`application/json`, """{"base64":"9OJ0OSDLSD3ryrt4567909876EFGHU45T6YUJKGF"}"""))
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(data => {
      data.unbind()
      Future {
        println(s"la data del future es: $data")
      }
    })
    .onComplete(_ => system.terminate())
}

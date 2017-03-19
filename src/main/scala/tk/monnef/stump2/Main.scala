package tk.monnef.stump2

import argonaut._
import Argonaut._
import ArgonautShapeless._
import org.http4s.HttpService
import org.http4s.MediaType._
import org.http4s.dsl.Root
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze._
import org.http4s.server.syntax._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import org.http4s.server.middleware._

import utils._

object Main extends ServerApp {
  val Port = 8022
  val Address = "localhost"
  var ripper: Ripper = _

  override def server(args: List[String]): Task[Server] = {
    println(s"initializing ripper")
    ripper = new Ripper
    println(s"starting server on http://$Address:$Port")
    BlazeBuilder
    .bindHttp(Port, Address)
    .mountService(genServices(), "/api")
    .mountService(genDocService(), "/")
    .start
  }

  private[this] val encodeArticleList = EncodeJson.of[List[ArticlePreview]]
  private[this] val encodeArticle = EncodeJson.of[Article]

  def formatJsonAndWrap(json: Json) = Ok(json.spaces2)

  private[this] def genServices() = {

    val articlesService = HttpService {
      case GET -> Root / "articles" =>
        //ripper.getArticleList() |> encodeArticleList |> (Ok _)
        //ripper.getArticleList() |> encodeArticleList.apply |> { x => x.spaces2 } |> { x => Ok(x) }
        ripper.getArticleList() |> encodeArticleList.apply |> formatJsonAndWrap

      case GET -> Root / "article" / url =>
        ripper.getArticle(url) |> encodeArticle.apply |> formatJsonAndWrap
    }
    val services = CORS(articlesService) // orElse articlesService
    services
  }

  private[this] def genDocService() = {
    HttpService {
      case GET -> Root =>
        Ok(
          """<html><body>
            |<a href='api/articles'>articles</a>
            |</body></html>
          """.stripMargin).asHtml()
    }
  }
}

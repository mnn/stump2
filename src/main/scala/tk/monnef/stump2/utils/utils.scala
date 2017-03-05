package tk.monnef.stump2

import org.http4s.Status
import org.http4s.headers.{`Content-Length`, `Content-Type`}
import org.http4s._
import org.http4s.MediaType._
import org.http4s.dsl._
import org.http4s.multipart._
import org.http4s.server._
import org.http4s.server.middleware.PushSupport._
import org.http4s.server.middleware.authentication._

import scala.util.Try
import scalaz.concurrent.Task

package object utils {

  val UtilsTest = 1

  implicit class StringPimps(val x: String) {
    def parseIntOpt: Option[Int] = Try(x.toInt).toOption
  }

  implicit class TestPimps(val x: Task[Response]) {
    def asHtml() = x.withContentType(Some(`Content-Type`(`text/html`)))
  }

}

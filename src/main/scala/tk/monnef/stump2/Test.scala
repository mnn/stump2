package tk.monnef.stump2

import scala.collection.JavaConverters._
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.http4s._, org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.server.syntax._
import argonaut._, Argonaut._, ArgonautShapeless._

object Test extends App {
  //  testJsoup()
  //  testSelenium()
  //  testHttp4s()
  testArgonaut()

  def testJsoup() {
    val doc = Jsoup.parse("<p id=\"x\">hello<strong class=\"y\"> world!</strong></p>")
    println(doc.select("p#x").text())
  }

  def testSelenium() {
    System.setProperty("webdriver.chrome.driver", "chromedriver")
    val driver = new ChromeDriver()
    driver.get("http://maslo.cz")
    val title = driver.findElement(By.cssSelector("h2")).getText
    println("title: " + title)
    driver.quit()
  }

  def testHttp4s() {
    val helloWorldService = HttpService {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }
    val services = helloWorldService
    val port = 8022
    val address = "localhost"
    val builder = BlazeBuilder.bindHttp(port, address).mountService(helloWorldService, "/").mountService(services, "/api")
    val server = builder.run
    println(s"server is running on http://$address:$port")
    Thread.sleep(Long.MaxValue)
  }

  case class ArgonautTestCaseClassA(children: List[ArgonautTestCaseClassB], title: String, body: Option[String])

  case class ArgonautTestCaseClassB(name: String)

  def testArgonaut() {
    val encode = EncodeJson.of[ArgonautTestCaseClassA]
    val cls = ArgonautTestCaseClassA(List(ArgonautTestCaseClassB("b1"), ArgonautTestCaseClassB("b2")), "tit", None)
    val encoded = encode(cls)
    println("encoded: " + encoded)
    val decode = DecodeJson.of[ArgonautTestCaseClassA]
    println("decoded: " + decode.decodeJson(encoded))
  }
}

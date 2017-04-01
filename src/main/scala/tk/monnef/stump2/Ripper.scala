package tk.monnef.stump2

import java.net.{URLDecoder, URLEncoder}

import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil

import scala.collection.JavaConverters._
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.{CapabilityType, DesiredCapabilities}
import com.github.nscala_time.time.Imports._
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}

import scalaz._
import Scalaz._
import utils._

import scala.util.Try

class Ripper {

  import Ripper._

  onCreate()

  private[this] var driver: ChromeDriver = _

  private[this] def onCreate(): Unit = {
    createDriver()
  }

  private[this] def createDriver(): Unit = {
    System.setProperty("webdriver.chrome.driver", "chromedriver")
    val proxy = new BrowserMobProxyServer
    proxy.start(0)
    val seleniumProxy = ClientUtil.createSeleniumProxy(proxy)
    val capabilities = new DesiredCapabilities()
    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy)
    List(
      "https?://trackad\\.cz.*",
      "https?://.*\\.bbelements\\.com.*",
      "https?://.*\\.gemius\\.pl.*",
      "https?://.*\\.doubleclick\\.net.*",
      "https?://.*\\.onaudience\\.com.*",
      "https?://.*\\.googlesyndication.com.*",
      "https?://.*\\.bkrtx.com.*",
      "https?://.*\\.getsitecontrol.com.*"
    ).foreach(x => proxy.blacklistRequests(x, 404))
    driver = new ChromeDriver(capabilities)
  }

  def getPageSourceCode(url: String): String = {
    driver.get(url)
    driver.getPageSource
  }

  def getParsedPage(url: String): Document = Jsoup.parse(getPageSourceCode(url), url)

  def getText(elem: Element, selector: String): String = elem.select(selector).text()

  def getUrl(elem: Element, selector: String): String = elem.select(selector).attr("href")

  def getImageUrl(elem: Element, selector: String): String = elem.select(selector).attr("src")

  def getLink(elem: Element, selector: String): NameUrlPair = NameUrlPair(
    getText(elem, selector),
    getUrl(elem, selector)
  )

  val articleListFmt = DateTimeFormat.forPattern("d. M. yyyy")

  def getParsedDate(rawDate: String): Option[DateTime] = {
    rawDate.toLowerCase.replace(" ", " ") match {
      case "dnes" => DateTime.now().some
      case "včera" => (DateTime.now() - 1.day).some
      case x => Try(DateTime.parse(x, articleListFmt)).toOption
    }
  }

  def getParsedDateString(rawDate: String): String = getParsedDate(rawDate).map(_.toString(ISODateTimeFormat.dateTime())).getOrElse("")

  def elemToArticlePreview(elem: Element): Option[ArticlePreview] = {
    val gTxt = getText(elem, _: String)
    val gUrl = getUrl(elem, _: String)
    val gIUrl = getImageUrl(elem, _: String)
    val gLink = getLink(elem, _: String)
    val name = gTxt("h3.article__heading, h3.opener__heading")
    val url = gUrl("a.link-block")
    val urlEncoded = URLEncoder.encode(url, "UTF-8")
    val perex = gTxt(".perex")
    val author = gLink(".impressum__author")
    val date = gTxt(".impressum__date")
    val category = gLink(".impressum__rubric")
    val commentsCount = gTxt(".comments__count").parseIntOpt

    val imageSrc = gIUrl(".article__img img, .opener__link-img img")
    val imageDataSrc = elem.select(".article__img img").attr("data-src")
    val imageUrl = if (imageDataSrc.isEmpty) imageSrc else imageDataSrc

    if (name.isEmpty) None
    else ArticlePreview(name, url, urlEncoded, perex, author, date, category, commentsCount, imageUrl, false, getParsedDateString(date)).some
  }

  def getArticleList(): List[ArticlePreview] = {
    val articleElems = getParsedPage(BaseUrl).select(".page-block--opener, .article.article--content").asScala
    articleElems.flatMap(elemToArticlePreview).toList
  }

  def getArticle(url: String): Article = {
    val decodedUrl = if (url.startsWith("%")) URLDecoder.decode(url, "UTF-8") else url
    val processedUrl = (if (decodedUrl.startsWith("/")) BaseUrl else "") + decodedUrl
    val doc = getParsedPage(processedUrl)
    val elems = doc.select(".content--detail")
    if (elems.size() != 1) println(s"problem with separating content of an article, found ${elems.size()}, expected 1.")
    val elem = elems.first()

    val gTxt = getText(elem, _: String)
    val gUrl = getUrl(elem, _: String)
    val gIUrl = getImageUrl(elem, _: String)
    val gLink = getLink(elem, _: String)

    val name = gTxt("h1.detail__heading")
    val imageUrl = getImageUrl(elem, ".perex__img img")
    val author = gLink(".perex__author a")
    val date = gTxt(".perex__date [itemprop='datePublished']")
    val perex = gTxt(".perex__text [itemprop='description']")
    val body = elem.select("[itemprop='articleBody']").html()

    Article(name, imageUrl, author, date, perex, body)
  }

  def getActualitiesList(): List[ArticlePreview] = {
    val articleElems = getParsedPage(BaseUrl).select(".box-actualities--details .article").asScala
    articleElems.flatMap(elemToArticlePreview).map(_.copy(isActuality = true)).toList
  }
}

object Ripper {
  val BaseUrl = "http://root.cz"
}

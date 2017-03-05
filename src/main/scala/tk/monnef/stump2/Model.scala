package tk.monnef.stump2

case class NameUrlPair(name: String, url: String)

case class ArticlePreview(
                           name: String,
                           url: String,
                           urlEncoded: String,
                           perex: String,
                           author: NameUrlPair,
                           date: String,
                           category: NameUrlPair,
                           commentsCount: Option[Int],
                           imageUrl: String
                         )

case class Article(
                    name: String,
                    imageUrl: String,
                    author: NameUrlPair,
                    date: String,
                    perex: String,
                    body: String
                  )
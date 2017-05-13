name := "stump2"

version := "1.0"

scalaVersion := "2.12.1"

val http4sVersion = "0.15.6"

resolvers += Resolver.sonatypeRepo("releases")

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

mainClass in(Compile, run) := Some("tk.monnef.stump2.Main")

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.10.2",
  "org.seleniumhq.selenium" % "selenium-java" % "3.2.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M4",
  "net.lightbody.bmp" % "browsermob-core" % "2.1.4",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "org.feijoas" %% "mango" % "0.14"
)

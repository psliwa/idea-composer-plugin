resolvers += Resolver.url("jetbrains-sbt", url("https://dl.bintray.com/jetbrains/sbt-plugins"))(
  Resolver.ivyStylePatterns
)

addSbtPlugin("org.jetbrains" % "sbt-idea-plugin" % "3.3.3")
addSbtPlugin("org.jetbrains" % "sbt-ide-settings" % "1.0.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.0")

resolvers += Resolver.url("dancingrobot84-bintray", url("http://dl.bintray.com/dancingrobot84/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.dancingrobot84" % "sbt-idea-plugin" % "0.4.2")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")
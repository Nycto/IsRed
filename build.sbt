name := "IsRed"

organization := "com.roundeights"

version := "0.2"

scalaVersion := "2.10.3"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

publishTo := Some("Spikemark" at "https://spikemark.herokuapp.com/maven/roundeights")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Application dependencies
libraryDependencies ++= Seq(
    "io.netty" % "netty" % "3.6.3.Final",
    "org.specs2" %% "specs2" % "2.3.4" % "test"
)


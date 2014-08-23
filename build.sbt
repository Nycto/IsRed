name := "IsRed"

organization := "com.roundeights"

version := "0.2"

scalaVersion := "2.11.2"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

publishTo := Some("Spikemark" at "https://spikemark.herokuapp.com/maven/roundeights")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Application dependencies
libraryDependencies ++= Seq(
    "io.netty" % "netty" % "3.9.1.Final",
    "org.specs2" %% "specs2" % "2.3.11" % "test"
)


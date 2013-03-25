name := "IsRed"

organization := "com.roundeights"

version := "0.1"

scalaVersion := "2.10.1"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

// Application dependencies
libraryDependencies ++= Seq(
    "io.netty" % "netty" % "3.6.3.Final",
    "org.specs2" %% "specs2" % "1.14" % "test"
)


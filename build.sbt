name := "odyssey"

version := "1.0"

scalaVersion := "2.9.2"

//libraryDependencies += "joda-time" %% "joda-time" % "2.2"

// this doesn't work with 2.10 for some reason
libraryDependencies += "org.scalaj" %% "scalaj-time" % "0.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"


name := "odyssey"

version := "1.0"

scalaVersion := "2.10.1"

// current joda time wrapper in scala
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "0.4.0"

// async HTTP library (wraps java's async-http-client)
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.10.1"

// SLICK
libraryDependencies += "com.typesafe.slick" %% "slick" % "1.0.0"

// scala test
libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"


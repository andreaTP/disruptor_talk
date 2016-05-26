
name := "disruptor_talk"

scalaVersion := "2.12.0-M4"

fork in run := true

javaOptions += "-Xmx12G"

libraryDependencies += "com.lmax" % "disruptor" % "3.3.4"

libraryDependencies +=
  "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1"
//"org.json4s" % "json4s-jackson_2.12.0-M3" % "3.3.0"
//"org.json4s" % "json4s-jackson" % "3.3.0"

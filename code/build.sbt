
name := "disruptor_talk"

// Dotty version
//scalaVersion := "0.1-SNAPSHOT"
scalaVersion := "2.12.0-M4"

fork in run := true

javaOptions += "-Xmx12G"

//scalaBinaryVersion := "2.11"

//autoScalaLibrary := false
    
//libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.5"

libraryDependencies += "com.lmax" % "disruptor" % "3.3.4"

//scalaCompilerBridgeSource := ("ch.epfl.lamp" % "dotty-bridge" % "0.1-SNAPSHOT" % "component").sources()
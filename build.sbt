name := """JianMu"""
organization := "com.tongjimobiml"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.0"

libraryDependencies += guice
libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8" % "provided"

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

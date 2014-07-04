name := "web-link-checker"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.3-SNAPSHOT"
  
libraryDependencies += "com.ning" % "async-http-client" % "1.7.5"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.3.3"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.3.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
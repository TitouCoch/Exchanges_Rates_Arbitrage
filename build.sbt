ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "Exchange_Rates_Arbitrage",
    libraryDependencies += "com.lihaoyi" %% "requests" % "0.8.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2",
    libraryDependencies += "org.openjfx" % "javafx-controls" % "16" % "compile"
  )

name := "shapeless-config-parse"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.typesafe" % "config" % "1.3.1",
  "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

scalacOptions := Seq("-unchecked", "-deprecation")


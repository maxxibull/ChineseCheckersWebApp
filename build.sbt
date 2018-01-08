import play.Project._

name := "websocket-chat"

version := "1.0"

javacOptions += "-Xlint:deprecation"

libraryDependencies += "junit" % "junit" % "4.12" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

playJavaSettings
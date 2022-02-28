scalaVersion := "3.1.1"

val TinyScalaUtilsJava = "com.github.charpov" %% "tiny-scala-utils-java" % "0.4.1"
val TinyScalaUtilsTest = "com.github.charpov" %% "tiny-scala-utils-test" % "0.4.1" % Test

resolvers += "TinyScalaUtils" at "https://charpov.github.io/TinyScalaUtils/maven/"

libraryDependencies ++= Seq(TinyScalaUtilsJava, TinyScalaUtilsTest)

Test / fork                 := true
Test / parallelExecution    := false
Test / run / outputStrategy := Some(StdoutOutput)

Compile / unmanagedSourceDirectories += baseDirectory.value / "src-instructor" / "main" / "scala"
Compile / unmanagedSourceDirectories += baseDirectory.value / "src-instructor" / "main" / "java"
Compile / unmanagedResourceDirectories += baseDirectory.value / "src-instructor" / "resources"
Test / unmanagedSourceDirectories += baseDirectory.value / "src-instructor" / "test" / "scala"
Test / unmanagedSourceDirectories += baseDirectory.value / "src-instructor" / "test" / "java"

scalacOptions ++= Seq(
  "-encoding",
  "utf-8",        // Specify character encoding used by source files.
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature",     // Emit warning for usages of features that should be imported explicitly.
  "-unchecked",   // Enable detailed unchecked (erasure) warnings.
)

javacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",  // Specify character encoding used by source files.
  "-Xlint", // Enable recommended additional warnings
)

name := "spark-ko-nlp"

organization in ThisBuild := "com.dongjin"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.11.8", "2.12.10")

val sparkVersion = "2.4.3"
val koalaVersion = "2.0.5"
val koalaScalaVersion = "2.0.2"
val scalatestVersion = "3.0.8"

val commonSettings = Seq(
  autoScalaLibrary := true,
  javacOptions := javacOptions.value ++ Seq("-source", "1.8", "-target", "1.8"),
  parallelExecution in Test := false,
  fork in Test := true,
  javaOptions := javaOptions.value ++ Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
  resolvers := resolvers.value ++ Seq("jitpack" at "https://jitpack.io/")
)

// root project
lazy val root = (project in file("."))
  .settings(publishArtifact := false)
  .aggregate(konlp, arirang, kmr)
  .dependsOn(konlp, arirang, kmr)

lazy val commonDependencies = Seq(
  "kr.bydelta" %% "koalanlp-scala" % koalaScalaVersion,
  // ("kr.bydelta" % "koalanlp-daon" % koalaScalaVersion artifacts Artifact("jar", "assembly")) % Test,
  // "kr.bydelta" % "koalanlp-eunjeon" % koalaScalaVersion % Test,
  // ("kr.bydelta" % "koalanlp-rhino" % koalaScalaVersion artifacts Artifact("jar", "assembly")) % Test,
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "com.holdenkarau" %% "spark-testing-base" % s"${sparkVersion}_0.12.0" % Test,
  "org.apache.spark" %% "spark-core" % sparkVersion % Test,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test
)

// konlp project
lazy val konlp = (project in file("konlp"))
  .settings(commonSettings,
    libraryDependencies := commonDependencies)

// arirang project
lazy val arirang = (project in file("arirang"))
  .settings(commonSettings,
    libraryDependencies := commonDependencies ++ Seq(
      ("kr.bydelta" % "koalanlp-arirang" % koalaScalaVersion artifacts Artifact("jar", "assembly")) % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// kmr project
lazy val kmr = (project in file("kmr"))
  .settings(commonSettings,
    libraryDependencies := commonDependencies ++ Seq(
      "kr.bydelta" % "koalanlp-kmr" % koalaScalaVersion % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// sbt-sonatype configuration
homepage := Some(url("https://github.com/dongjinleekr/spark-ko-nlp"))
scmInfo := Some(ScmInfo(url("https://github.com/dongjinleekr/spark-ko-nlp"),
  "git@github.com:dongjinleekr/spark-ko-nlp.git"))
developers := List(Developer("username",
  "Lee Dongjin",
  "dongjin@apache.org",
  url("https://github.com/dongjinleekr")))
licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

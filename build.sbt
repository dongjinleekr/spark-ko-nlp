name := "spark-ko-nlp-parent"

ThisBuild / organization := "com.dongjinlee"

ThisBuild / version := "0.6-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

ThisBuild / crossScalaVersions := Seq("2.11.8", "2.12.10")

ThisBuild / autoScalaLibrary := true

lazy val commonSettings = Seq(
  javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  Test / parallelExecution := false,
  Test / fork := true,
  resolvers ++= Seq("jitpack" at "https://jitpack.io/")
)

lazy val dependencies =
  new {
    val sparkVersion = "2.4.3"
    val koalaVersion = "2.0.5"
    val koalaScalaVersion = "2.0.2"
    val scalatestVersion = "3.0.8"

    val koalaNLP = "kr.bydelta" %% "koalanlp-scala" % koalaScalaVersion
    val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
    val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
    val sparkTestingBase = "com.holdenkarau" %% "spark-testing-base" % s"${sparkVersion}_0.12.0"
    val koalaArirang = "kr.bydelta" % "koalanlp-arirang" % koalaScalaVersion artifacts Artifact("jar", "assembly")
    val koalaDaon = "kr.bydelta" % "koalanlp-daon" % koalaScalaVersion artifacts Artifact("jar", "assembly")
    val koalaKmr = "kr.bydelta" % "koalanlp-kmr" % koalaScalaVersion
    val koalaRhino = "kr.bydelta" % "koalanlp-rhino" % koalaScalaVersion artifacts Artifact("jar", "assembly")
    val scalaTest = "org.scalatest" %% "scalatest" % scalatestVersion
  }

lazy val commonDependencies = Seq(
  dependencies.koalaNLP,
  dependencies.sparkCore % Provided,
  dependencies.sparkSQL % Provided,
  dependencies.sparkTestingBase % Test,
  dependencies.sparkCore % Test,
  dependencies.sparkSQL % Test,
  dependencies.scalaTest % Test
)

// root project
lazy val root = (project in file("."))
  .settings(publishArtifact := false)
  .aggregate(konlp, arirang, daon, kmr, rhino)
  .dependsOn(konlp, arirang, daon, kmr, rhino)

// konlp project
lazy val konlp = (project in file("konlp"))
  .settings(commonSettings ++ { name := "spark-ko-nlp" },
    libraryDependencies ++= commonDependencies)

// arirang project
lazy val arirang = (project in file("arirang"))
  .settings(commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.koalaArirang % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// daon project
lazy val daon = (project in file("daon"))
  .settings(commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.koalaDaon % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// kmr project
lazy val kmr = (project in file("kmr"))
  .settings(commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.koalaKmr % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// rhino project
lazy val rhino = (project in file("rhino"))
  .settings(commonSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.koalaRhino % Test
    ),
    publishArtifact := false)
  .dependsOn(konlp % "test->test")

// sbt-sonatype configuration
ThisBuild / homepage := Some(url("https://github.com/dongjinleekr/spark-ko-nlp"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/dongjinleekr/spark-ko-nlp"),
  "git@github.com:dongjinleekr/spark-ko-nlp.git"))
ThisBuild / developers := List(Developer("dongjinlee",
  "Lee Dongjin",
  "dongjin@apache.org",
  url("https://github.com/dongjinleekr")))
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

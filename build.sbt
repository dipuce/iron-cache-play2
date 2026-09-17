// iron-cache-play2 — Iron.io cache module for Play Framework 3.x
//
// Cross-built for Scala 2.13 and Scala 3 (LTS). `sbt +test` runs the test
// suite against every Scala version; `sbt +publishLocal` publishes both.

val playVersion  = "3.0.11"
val scala213     = "2.13.18"
val scala3       = "3.3.8"
val scalaTestVer = "3.2.20"

ThisBuild / organization         := "com.dipuce"
ThisBuild / organizationName     := "Dipuce LLC"
ThisBuild / organizationHomepage := Some(url("https://www.dipuce.com"))
// Set explicitly (overriding sbt-dynver from sbt-ci-release) so the version is
// visible here: release commits carry the bare version and are tagged with it.
ThisBuild / version              := "4.0.0-SNAPSHOT"
// sbt-dynver (via sbt-ci-release) would otherwise derive these from git and expects
// v-prefixed tags; this repo tags bare versions, so key both off the version above.
ThisBuild / isSnapshot           := version.value.endsWith("-SNAPSHOT")
ThisBuild / dynverVTagPrefix     := false
// The Central Portal snapshot repository rejects uploaded checksum files (403 on *.md5);
// it computes them server-side.
ThisBuild / publish / checksums  := Nil
ThisBuild / scalaVersion         := scala3
ThisBuild / crossScalaVersions   := Seq(scala213, scala3)
ThisBuild / versionScheme        := Some("early-semver")

ThisBuild / homepage := Some(url("https://github.com/dipuce/iron-cache-play2"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/dipuce/iron-cache-play2"),
    "scm:git:git@github.com:dipuce/iron-cache-play2.git"
  )
)
ThisBuild / developers := List(
  Developer("moneymikeMD", "Mike Garrett", "mike@dipuce.com", url("https://github.com/moneymikeMD"))
)

def compilerOptions(scalaVer: String): Seq[String] =
  Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8") ++
    (CrossVersion.partialVersion(scalaVer) match {
      case Some((3, _)) => Seq("-Wunused:imports")
      case _            => Seq("-Xlint", "-Wunused:imports", "-Xsource:3")
    })

lazy val root = (project in file("."))
  .settings(
    name := "iron-cache-play2",
    description := "A Play Framework cache module backed by Iron.io's IronCache",
    scalacOptions ++= compilerOptions(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.playframework" %% "play-cache" % playVersion,
      "org.playframework" %% "play-ws"    % playVersion,
      "org.playframework" %% "play-test"              % playVersion  % Test,
      "org.playframework" %% "play-pekko-http-server" % playVersion  % Test,
      "org.playframework" %% "play-ahc-ws" % playVersion  % Test,
      "org.playframework" %% "play-guice"  % playVersion  % Test,
      "org.scalatest"     %% "scalatest"   % scalaTestVer % Test
    ),
    Test / fork := true
  )

// Minimal Play application exercising the module. Not published.
lazy val sample = (project in file("sample"))
  .enablePlugins(PlayScala)
  .dependsOn(root)
  .settings(
    name := "iron-cache-sample",
    publish / skip := true,
    libraryDependencies ++= Seq(guice, ws)
  )

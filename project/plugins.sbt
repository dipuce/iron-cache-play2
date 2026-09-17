// Only needed for the `sample` Play application; the library itself is plain sbt.
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.11")

// Signing + publishing to Maven Central (Sonatype Central Portal). See README "Releasing".
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.12.1")

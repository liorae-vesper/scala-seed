import java.time.Duration
import net.nmoncho.sbt.osv.settings.{EngineSettings, ReportGenerator, ScopesSettings}

ThisBuild / scalaVersion := "3.10.1"
ThisBuild / organization := "io.github.john-eevee"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val dependencyManifest = taskKey[String]("Hash all resolved application and test JARs")
lazy val writeDependencyManifest = taskKey[Unit]("Write the reviewed dependency integrity baseline")
lazy val verifyDependencies =
  taskKey[Unit]("Require matching dependency coordinates and JAR hashes")

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-next-sbt-template",
    dependencyManifest := Def.uncached {
      val report = update.value
      val entries = for {
        configuration <- report.configurations
        module <- configuration.modules if !module.evicted
        (artifact, jar) <- module.artifacts if artifact.extension == "jar"
      } yield {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest
          .digest(java.nio.file.Files.readAllBytes(jar.toPath))
          .map(byte => f"${byte & 0xff}%02x")
          .mkString
        s"${module.module.organization}:${module.module.name}:${module.module.revision}\t$hash"
      }
      entries.distinct.sorted.mkString("", "\n", "\n")
    },
    writeDependencyManifest := Def.uncached {
      IO.write(baseDirectory.value / "project" / "dependencies.sha256", dependencyManifest.value)
    },
    verifyDependencies := Def.uncached {
      val baseline = baseDirectory.value / "project" / "dependencies.sha256"
      val actual = dependencyManifest.value
      if (!baseline.isFile || IO.read(baseline) != actual)
        sys.error("Dependency integrity mismatch; review changes and run writeDependencyManifest")
    },
    publish / skip := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-release:21",
      "-source:3.10",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Werror",
      "-Wunused:all",
      "-Wvalue-discard",
      "-Wnonunit-statement",
      "-Wsafe-init"
    ),
    javacOptions ++= Seq("--release", "21", "-Xlint:all", "-Werror"),
    evictionErrorLevel := Level.Error,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.6" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.3.1" % Test
    ),
    Test / testOptions += Tests.Argument("+l"),
    osvScopes := ScopesSettings(test = true),
    osvFailBuildOnCVSS := -1.0,
    osvOutputDirectory := target.value / "security",
    osvReportFormats := Seq(ReportGenerator.HTML, ReportGenerator.JSON),
    osvEngineSettings := EngineSettings.Default.copy(
      cacheEviction = Duration.ZERO,
      dataDirectory = Some(target.value / "osv-cache")
    ),
    osvAnalysisTimeout := Some(Duration.ofMinutes(5)),
    osvConnectionTimeout := Some(Duration.ofSeconds(30)),
    osvConnectionReadTimeout := Some(Duration.ofSeconds(60))
  )

addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
addCommandAlias(
  "check",
  ";verifyDependencies;scalafmtCheckAll;scalafmtSbtCheck;clean;testOnly *;package"
)
addCommandAlias("audit", "osvScan all-projects single-report list-unused-suppressions")

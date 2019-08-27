import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("[_\\.\\W+]", "-")
val appVersion = conf.getString("app.version")

sbtPlugin    := true
scalaVersion := "2.13.0"

// Custom Maven repository
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin).settings(
    name         := appName,
    version      := appVersion,
    organization := "com.github.btnguyen2k",
    resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
)

/*----------------------------------------------------------------------*/
// Convenient settings: mostly copied from src/g8/build.sbt
/*----------------------------------------------------------------------*/
maintainer := "btnguyen2k@gmail.com"

/* Docker packaging options */
// Manual docker build:
//// 1. dir$ sbt clean docker:stage
//// 2. dir$ docker build --force-rm --squash -t microservices-undertow-seed:2.0.r1 ./target/docker/stage
// Auto docker build (local):
//// 1. dir$ sbt clean docker:publishLocal
dockerCommands := Seq()
import com.typesafe.sbt.packager.docker._
dockerCommands := Seq(
  Cmd("FROM"          , "openjdk:11-jre-slim"),
  Cmd("LABEL"         , "maintainer=\"Thanh Nguyen\""),
  Cmd("ADD"           , "opt /opt"),
  Cmd("RUN"           , "apk add --no-cache -U bash ca-certificates tzdata && ln -s /opt/docker /opt/" + appName + " && chown -R daemon:daemon /opt && chmod 755 /opt/docker/conf/*.sh && chmod 755 /opt/docker/bin/*"),
  Cmd("RUN"           , "cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime"),
  Cmd("WORKDIR"       , "/opt/" + appName),
  Cmd("USER"          , "daemon"),
  ExecCmd("ENTRYPOINT", "./bin/" + appName)
)
packageName in Docker := appName
version in Docker     := appVersion

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
fork := false
val _mainClass = "com.github.btnguyen2k.mus.Bootstrap"

/* Packaging options */
mainClass in (Compile, packageBin)       := Some(_mainClass)
sources in (Compile, doc)                := Seq.empty
publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false
autoScalaLibrary                         := false

// add conf/ directory: https://www.scala-sbt.org/sbt-native-packager/formats/universal.html#mappingshelper
import NativePackagerHelper._
mappings in Universal ++= directory(baseDirectory.value / "conf")

/* Compiling options */
javacOptions ++= Seq("-source", "11", "-target", "11", "-encoding", "UTF-8")

/* Run options */
javaOptions  ++= collection.JavaConverters.propertiesAsScalaMap(System.getProperties)
  .map{ case (key,value) => "-D" + key + "=" +value }.toSeq
mainClass in (Compile, run) := Some(_mainClass)

/* Eclipse settings */
EclipseKeys.preTasks                 := Seq(compile in Compile)                     // Force compile project before running the eclipse command
EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.projectFlavor            := EclipseProjectFlavor.Java                   // Java project. Don't expect Scala IDE
//EclipseKeys.executionEnvironment     := Some(EclipseExecutionEnvironment.JavaSE18)  // No Java11 yet, manually switch to JDK11 in Eclipse!
// Use .class files instead of generated .scala files for views and routes
//EclipseKeys.createSrc                := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)

/* Dependencies */
val _slf4jVersion       = "1.7.28"
val _undertowVersion    = "2.0.25.Final"
val _jacksonVersion     = "2.9.9"
val _ddthCommonsVersion = "1.1.0"
val _ddthRecipesVersion = "1.0.0"

libraryDependencies ++= Seq(
    "org.slf4j"                     % "slf4j-api"                     % _slf4jVersion
   ,"org.slf4j"                     % "log4j-over-slf4j"              % _slf4jVersion
   ,"ch.qos.logback"                % "logback-classic"               % "1.2.3"

   ,"org.apache.commons"            % "commons-lang3"                 % "3.9"
   ,"com.typesafe"                  % "config"                        % "1.3.4"

   ,"io.undertow"                   % "undertow-core"                 % _undertowVersion
   ,"io.undertow"                   % "undertow-websockets-jsr"       % _undertowVersion

   ,"com.fasterxml.jackson.core"    % "jackson-core"                  % _jacksonVersion
   ,"com.fasterxml.jackson.core"    % "jackson-databind"              % _jacksonVersion

   ,"com.github.ddth"               % "ddth-commons-core"             % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-commons-serialization"    % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-commons-typesafeconfig"   % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-recipes"                  % _ddthRecipesVersion
)

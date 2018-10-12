import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("[_\\\\.\\\\W+]", "-")
val appVersion = conf.getString("app.version")

sbtPlugin    := true
scalaVersion := "$scala_version$"
//giter8.ScaffoldPlugin.projectSettings

// Custom Maven repository
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin).settings(
    name         := appName,
    version      := appVersion,
    organization := "$organization$",
    resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
)

/*----------------------------------------------------------------------*/

// Eclipse configurations
EclipseKeys.preTasks                 := Seq(compile in Compile)                     // Force compile project before running the eclipse command
EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.projectFlavor            := EclipseProjectFlavor.Java                   // Java project. Don't expect Scala IDE
EclipseKeys.executionEnvironment     := Some(EclipseExecutionEnvironment.JavaSE18)  // expect Java 1.8
// Use .class files instead of generated .scala files for views and routes
//EclipseKeys.createSrc                := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)


/* Docker packaging options */
// Manual docker build:
//// 1. dir\$ sbt clean docker:stage
//// 2. dir\$ docker build --force-rm --squash -t $name$:$app_version$ ./target/docker/stage
// Auto docker build (local):
//// 1. dir\$ sbt clean docker:publishLocal
dockerCommands := Seq()
import com.typesafe.sbt.packager.docker._
dockerCommands := Seq(
  Cmd("FROM"          , "openjdk:8-jre-alpine"),
  Cmd("LABEL"         , "maintainer=\"$app_author$\""),
  Cmd("ADD"           , "opt /opt"),
  Cmd("RUN"           , "apk add --no-cache -U tzdata bash && ln -s /opt/docker /opt/" + appName + " && chown -R daemon:daemon /opt && chmod 755 /opt/docker/conf/*.sh && chmod 755 /opt/docker/bin/*"),
  Cmd("RUN"           , "cp /usr/share/zoneinfo/$timezone$ /etc/localtime"),
  Cmd("WORKDIR"       , "/opt/" + appName),
  Cmd("USER"          , "daemon"),
  ExecCmd("ENTRYPOINT", "./conf/server-docker.sh", "start")
)
packageName in Docker := appName
version in Docker     := appVersion


sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
javacOptions    ++= Seq("-source", "1.8", "-target", "1.8")
fork := false
val _mainClass = "com.github.btnguyen2k.mus.Bootstrap"


/* Packaging options */
mainClass in (Compile, packageBin)       := Some(_mainClass)
sources in (Compile, doc)                := Seq.empty
publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false
autoScalaLibrary                         := false
// add conf/ directory
mappings in Universal                    ++= (baseDirectory.value / "conf" * "*" get) map(x => x -> ("conf/" + x.getName))


/* Compiling  options */
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")


/* Run options */
javaOptions  ++= collection.JavaConverters.propertiesAsScalaMap(System.getProperties)
  .map{ case (key,value) => "-D" + key + "=" +value }.toSeq
mainClass in (Compile, run) := Some(_mainClass)


/* Eclipse settings */
EclipseKeys.projectFlavor                := EclipseProjectFlavor.Java                   // Java project. Don't expect Scala IDE
EclipseKeys.executionEnvironment         := Some(EclipseExecutionEnvironment.JavaSE18)  // expect Java 1.8


/* Dependencies */
val _slf4jVersion       = "1.7.25"
val _undertowVersion    = "2.0.13.Final"
val _jacksonVersion     = "2.9.7"
val _ddthCommonsVersion = "0.9.1.7"
val _ddthRecipesVersion = "0.2.0.1"

libraryDependencies ++= Seq(
    "org.slf4j"                     % "slf4j-api"                     % _slf4jVersion
   ,"org.slf4j"                     % "log4j-over-slf4j"              % _slf4jVersion
   ,"ch.qos.logback"                % "logback-classic"               % "1.2.3"

   ,"org.apache.commons"            % "commons-lang3"                 % "3.8.1"
   ,"com.typesafe"                  % "config"                        % "1.3.3"

   ,"io.undertow"                   % "undertow-core"                 % _undertowVersion
   ,"io.undertow"                   % "undertow-websockets-jsr"       % _undertowVersion

   ,"com.fasterxml.jackson.core"    % "jackson-core"                  % _jacksonVersion
   ,"com.fasterxml.jackson.core"    % "jackson-databind"              % _jacksonVersion

   ,"com.github.ddth"               % "ddth-commons-core"             % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-commons-serialization"    % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-recipes"                  % _ddthRecipesVersion
)

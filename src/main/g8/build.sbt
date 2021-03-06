import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("[_\\\\.\\\\W+]", "-")
val appVersion = conf.getString("app.version")

sbtPlugin    := true
scalaVersion := "$scala_version$"

// Custom Maven repository
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging, DockerPlugin).
  settings(
    name         := appName,
    version      := appVersion,
    organization := "$organization$",
    resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
)

/*----------------------------------------------------------------------*/
maintainer := "$app_author$"

/* Docker packaging options */
// Manual docker build:
//// 1. dir\$ sbt clean docker:stage
//// 2. dir\$ docker build --force-rm --squash -t $name$:$app_version$ ./target/docker/stage
// Auto docker build (local):
//// 1. dir\$ sbt clean docker:publishLocal
dockerCommands := Seq()
import com.typesafe.sbt.packager.docker._
dockerCommands := Seq(
  Cmd("FROM"          , "openjdk:11-jre-slim"),
  Cmd("LABEL"         , "maintainer=\"$app_author$\""),
  Cmd("ADD"           , "opt /opt"),
  //Cmd("RUN"           , "apk add --no-cache -U bash ca-certificates tzdata && ln -s /opt/docker /opt/" + appName + " && chown -R daemon:daemon /opt && chmod 755 /opt/docker/conf/*.sh && chmod 755 /opt/docker/bin/*"),
  Cmd("RUN"           , "apt-get install bash ca-certificates tzdata && ln -s /opt/docker /opt/" + appName + " && chown -R daemon:daemon /opt && chmod 755 /opt/docker/conf/*.sh && chmod 755 /opt/docker/bin/*"),
  Cmd("RUN"           , "cp /usr/share/zoneinfo/$timezone$ /etc/localtime"),
  Cmd("WORKDIR"       , "/opt/" + appName),
  Cmd("USER"          , "daemon"),
  Cmd("EXPOSE"        , conf.getString("api.http.port")),
  ExecCmd("ENTRYPOINT", "./conf/server-docker.sh", "start")
)
dockerExposedPorts    := Seq(conf.getInt("api.http.port"))
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

/* Compiling  options */
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
val _undertowVersion    = "2.0.26.Final"
val _jacksonVersion     = "2.9.9"
val _springVersion      = "5.1.9.RELEASE"
val _ddthCommonsVersion = "1.1.0"
val _ddthDao            = "1.1.1"
val _ddthQueue          = "1.0.0"
val _ddthRecipesVersion = "1.1.0"

libraryDependencies ++= Seq(
    "org.slf4j"                     % "slf4j-api"                     % _slf4jVersion
   ,"org.slf4j"                     % "log4j-over-slf4j"              % _slf4jVersion
   ,"ch.qos.logback"                % "logback-classic"               % "1.2.3"

   // RDMBS JDBC drivers & Connection Pool
   ,"com.zaxxer"                    % "HikariCP"                      % "3.3.1"
   //// comment out or remove unused JDBC drivers
   ,"org.hsqldb"                    % "hsqldb"                        % "2.5.0"
   //,"mysql"                         % "mysql-connector-java"          % "8.0.17"
   //,"org.postgresql"                % "postgresql"                    % "42.2.6"
   //,"com.microsoft.sqlserver"       % "mssql-jdbc"                    % "7.4.1.jre11"

   ,"org.apache.commons"            % "commons-lang3"                 % "3.9"
   ,"com.typesafe"                  % "config"                        % "1.3.4"

   ,"io.undertow"                   % "undertow-core"                 % _undertowVersion
   //,"io.undertow"                   % "undertow-websockets-jsr"       % _undertowVersion

   ,"com.fasterxml.jackson.core"    % "jackson-core"                  % _jacksonVersion
   ,"com.fasterxml.jackson.core"    % "jackson-databind"              % _jacksonVersion

   ,"org.springframework"           % "spring-beans"                  % _springVersion
   ,"org.springframework"           % "spring-expression"             % _springVersion
   ,"org.springframework"           % "spring-context"                % _springVersion

   ,"com.github.ddth"               % "ddth-commons-core"             % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-commons-serialization"    % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-commons-typesafeconfig"   % _ddthCommonsVersion
   ,"com.github.ddth"               % "ddth-dao-core"                 % _ddthDao
   ,"com.github.ddth"               % "ddth-dao-jdbc"                 % _ddthDao
   ,"com.github.ddth"               % "ddth-queue-core"               % _ddthQueue
   ,"com.github.ddth"               % "ddth-queue-rocksdb"            % _ddthQueue

   ,"com.github.ddth"               % "ddth-recipes"                  % _ddthRecipesVersion
   ,"org.influxdb"                  % "influxdb-java"                 % "2.15"
   ,"org.elasticsearch.client"      % "elasticsearch-rest-high-level-client" % "7.3.1"

   ,"org.webjars"                   % "webjars-locator-core"          % "0.40"
   ,"org.webjars"                   % "swagger-ui"                    % "3.23.5"
   ,"io.swagger.core.v3"            % "swagger-annotations"           % "2.0.9"
)

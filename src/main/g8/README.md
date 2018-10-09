# $name$

$desc$ by $organization$, based from [gearmand-worker-java.g8](https://github.com/btnguyen2k/gearmand-worker-java.g8).

Copyright (C) by $organization$.

Latest release version: `0.1.0`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

## Usage

Generate Eclipse project with: `sbt eclipse`

Build standalone `.zip` distribution package: `sbt universal:packageBin`

Build Docker package: `sbt docker:stage`

Build Docker image and publish to local: `sbt docker:publishLocal`

**Configuration file**

Application's configuration file: `conf/application.conf`.

Important configs:
- Gearman servers: list of Gearman servers to connect to
  - Config key: `gearman.servers`
  - Type: `string`
  - Format: `host1:port1,host2,host3:port3,...` (if no post is specified, assume default port `4830` is used)
  - Example: `localhost,192.168.1.1:4731`
  - Alternative: list of Gearmand servers can be set via system variable `GEARMAN_SERVERS`
- Gearman functions: list of Gearman functions to subscribe
  - Config key `gearman.functions`
  - Type: `array of strings`
  - Example `["function1","function2"]`
- Job handlers: job handler settings
  - Config key `gearman.handlers`
  - Type: `map`
  - Format: `{function_name = fully_qualified_class_name}`
  - Example: `{
    function1=com.github.btnguyen2k.gearmanworker.samples.HandlerForFunction1
    function2=com.github.btnguyen2k.gearmanworker.samples.HandlerForFunction2
  }`
  - Special mapping `{_ = class_name}` will handle jobs from all functions if no other rules found.

See more: http://www.scala-sbt.org/sbt-native-packager/formats/universal.html

**Standalone application**

Standalone application (in `.zip` format)can be built via command `sbt universal:packageBin`. The generated `.zip` file will be created under directory `target/universal`.

Unzip the package and start the application with: `sh conf/server-prod.sh start`

Stop the application with: `sh conf/server-prod.sh stop`

**Docker Image**

Docker image can be build in 2 ways:
- Build Docker files with `sbt docker:stage`, generated files are placed under directory `target/docker`
- Build Docker image and publish to local with `sbt docker:publishLocal`

See more: http://www.scala-sbt.org/sbt-native-packager/formats/docker.html

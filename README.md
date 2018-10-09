# gearmand-worker-java.g8

Giter8 template for Gearmand Worker in Java.

To create a new project from template:

```
sbt new btnguyen2k/gearmand-worker-java.g8
```

Latest release: [template-v0.1.0](RELEASE-NOTES.md).

## Features

- Build and package project with `sbt`:
  - `sbt eclipse`: generate Eclipse project
  - `sbt run`: run project (for development)
  - `sbt universal:packageBin`: package project as a `.zip` file
  - `sbt docker:publishLocal`: package project as docker image and publish to local
- Support multiple Gearman servers
- Support multiple functions
- 2 types of job handlers:
  - `RunAllJobHandler`: accept and run all incoming jobs
  - `RunIfNotBusyJobHandler`: accept and run incoming jobs if not busy
- Samples job handler implementations (see [`com.github.btnguyen2k.gearmanworker.samples`](src/main/java/com/github/btnguyen2k/gearmanworker/samples)

## Configurations

Application's main configuration file `conf/application.conf` in [HOCON format](https://github.com/lightbend/config/blob/master/HOCON.md).

Important configurations:

- `gearman.servers`: list of Gearmand servers to connect to
  - Format: a string `"host1[:port1],host2[:port2]..."`
  - If no `port` is provided, the default port (`4730`) is used.
  - Example: `"localhost,10.0.0.1:4730,192.168.1.1:4730"`
- `gearman.functions`: list of functions to subscribe to
  - Format: a list of strings `["function1","function2"]...`
  - Example: `["demo","demo-again"]`
- `gearman.handlers`: define job handlers
  - Format: a map of `{function_name = class_name}`
  - Special mapping `{_ = class_name}` will handle jobs from all functions if no other rules found
  - Example:
```
handlers {
    _         = com.github.btnguyen2k.gearmanworker.samples.NoopRunAllJobHandler
    function1 = com.github.btnguyen2k.gearmanworker.samples.HandlerForFunction1
    function2 = com.github.btnguyen2k.gearmanworker.samples.HandlerForFunction2
}
```

## LICENSE & COPYRIGHT

See [LICENSE.md](LICENSE.md) for details. Copyright (c) 2018 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.

## Giter8 template. 

For information on giter8 templates, please see http://www.foundweekends.org/giter8/

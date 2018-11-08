# microservices-undertow-seed.g8

Giter8 template to develop microservices using Undertow framework.

To create a new project from template:

```
sbt new btnguyen2k/microservices-undertow-seed.g8
```

Latest release: [template-v2.0.r1](RELEASE-NOTES.md).

## Features

- Build and package project with `sbt`:
  - `sbt eclipse`: generate Eclipse project
  - `sbt run`: run project (for development)
  - `sbt universal:packageBin`: package project as a `.zip` file
  - `sbt docker:publishLocal`: package project as docker image and publish to local
- Start/Stop scripts (Linux shell scripts):
  - `start-dev.sh` for development environment
  - `conf/server-prod.sh` for production environment
  - `conf/server-docker.sh` for docker environment
- Samples APIs (see [`com.github.btnguyen2k.mus.samples`](src/main/java/com/github/btnguyen2k/mus/samples)

## Configurations

Application's main configuration file `conf/application.conf` in [HOCON format](https://github.com/lightbend/config/blob/master/HOCON.md).

Important configurations:

```
# application's name and version
app {
    version   = "app's version"
    name      = "app's name"
    shortname = "app's shortname"
    fullname  = "app's full/long name"
    desc      = "app's description"
}
```

```
# API configuirations
api {
    #API routings: map a URI to an API handler
    routes {
        "/api/samples/echo" {
            #list of HTTP methods, "*" means "allow all methods"
            allowed_methods = ["get","post"]
            handler         = "echo"
        }
        "/api/samples/info" {
            #list of HTTP methods, "*" means "allow all methods"
            allowed_methods = ["*"]
            handler         = "info"
        }
    }

    #API handlers: <handler name> maps to <API class name>
    #API must implement interface com.github.ddth.recipes.apiservice.IApiHandler
    handlers {
        echo = com.github.btnguyen2k.mus.samples.apihandlers.EchoApi
        info = com.github.btnguyen2k.mus.samples.apihandlers.InfoApi
    }
}
```


## Start/Stop Scripts

Commands:

- Start: `sh conf/server-prod.sh start`
- Stop : `sh conf/server-prod.sh stop`

//TODO


## Docker support

Application can be packaged as a Docker image.

1- Build and Publish Docker image locally

```shell
sbt docker:publishLocal
```

The command will build Docker image `$name$:$app_version$`.

2- Build Docker image manually (more control over the final Docker image)

Build project and generate necessary files to build Docker image (include `Dockerfile`)

```shell
sbt docker:stage
```

The command will create necessary files under directory `./target/docker/`

The generated `Dockerfile` is ready-to-go but you are free to inspect and change it. Once you are happy, build Docker image normally, sample command:

```shell
docker build --force-rm --squash -t $name$:$app_version$ ./target/docker/stage
```


## LICENSE & COPYRIGHT

See [LICENSE.md](LICENSE.md) for details. Copyright (c) 2018 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.

## Giter8 template

For information on giter8 templates, please see http://www.foundweekends.org/giter8/

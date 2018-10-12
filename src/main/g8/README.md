# $name$

$desc$ by $organization$, based on [microservices-undertow-seed.g8](https://github.com/btnguyen2k/microservices-undertow-seed.g8).

Copyright (C) by $organization$.

Latest release version: `0.1.0`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

## Usage

Generate Eclipse project with: `sbt eclipse`

Build standalone `.zip` distribution package: `sbt universal:packageBin`

Build Docker package: `sbt docker:stage`

Build Docker image and publish to local: `sbt docker:publishLocal`


### Configurations

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

### Start/Stop Scripts

Commands:

- Start: `sh conf/server-prod.sh start`
- Stop : `sh conf/server-prod.sh stop`

//TODO


### Standalone application

Standalone application (in `.zip` format)can be built via command `sbt universal:packageBin`. The generated `.zip` file will be created under directory `target/universal`.

Unzip the package and start the application with: `sh conf/server-prod.sh start`

Stop the application with: `sh conf/server-prod.sh stop`


### Docker support

Application can be packaged as a Docker image.

1- Build and Publish Docker image locally

```shell
sbt docker:publishLocal
```

The command will build Docker image `$name$:$version$`.

2- Build Docker image manually (more control over the final Docker image)

Build project and generate necessary files to build Docker image (include `Dockerfile`)

```shell
sbt docker:stage
```

The command will create necessary files under directory `./target/docker/`

The generated `Dockerfile` is ready-to-go but you are free to inspect and change it. Once you are happy, build Docker image normally, sample command:

```shell
docker build --force-rm --squash -t $name$:$version$ ./target/docker/stage
```

See more: http://www.scala-sbt.org/sbt-native-packager/formats/docker.html


## LICENSE & COPYRIGHT

See [LICENSE.md](LICENSE.md).

Third party libraries are distributed under their own licenses.

## Giter8 template

For information on giter8 templates, please see http://www.foundweekends.org/giter8/

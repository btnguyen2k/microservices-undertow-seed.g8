# microservices-undertow-seed.g8

Giter8 template to develop microservices using Undertow framework.

To create a new project from template:

```
sbt new btnguyen2k/microservices-undertow-seed.g8
```

Latest release: [template-v2.0.r4](RELEASE-NOTES.md).

## Features

- Build and package project with `sbt`:
  - `sbt eclipse`: generate Eclipse project
  - `sbt run`: run project (for development)
  - `sbt universal:packageBin`: package project as a `.zip` file
  - [Build Docker image](#docker-support).
- [Start/Stop scripts for Linux](#startstop-scripts):
  - `start-dev.sh` for development environment
  - `conf/server-prod.sh` for production environment
  - `conf/server-docker.sh` for docker environment
- Scaffold:
  - [`PetStore` sample APIs](#scaffolding)
- [Custom additional bootstrapping routines](#additional-bootstrapping-routines).
- WebJars support:
  - WebJar assets are accessed via `/webjar/{bundle}/*`
- API logging (since [template-v2.0.r4](RELEASE-NOTES.md))


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
    #Name of HTTP header that holds "application id" info passed from client.
    http_header_app_id = "X-App-Id"

    #Name of HTTP header that holds "access token" info passed from client.
    http_header_access_token = "X-Access-Token"

    # Scan these packages and build endpoints and handlers from annotations
    scan_packages = ["com.github.btnguyen2k.mus"]

    # API endpoints
    endpoints {
        # example of path "/api/petstore/pet/{id}"
        #"path" {
        #    # "http-method" is one of valid http method (e.g. GET, POST, PUT, etc), or "*" means "any method"
        #    # "handlerName" is a name referencing to api.handler configurations
        #    "http-method" = "handlerName"
        #}

        # See api_samples.conf for examples
    }

    # API handlers: required to build ApiRouter
    handlers {
        # <handler name> maps to <API class name>
        # API must implement interface com.github.ddth.recipes.apiservice.IApiHandler
        #handlerName = "Java class name"

        # See api_samples.conf for examples
    }
}
```

## Scaffolding

This template includes a Petstore API example as scaffold. See [document to generate the example](.g8/petstore/README.md).

## Implement APIs

Each API must implement `com.github.ddth.recipes.apiservice.IApiHandler`. Then, API handlers and endpoints can be defined and registered via *application configuration* or *annotation*.

**Define and register APIs via application configuration**

API handlers are configured via config-key `api.handlers`:

```
api {
    handlers {
        #handlerName = "Java class name"
        memInfo     = "com.github.btnguyen2k.mus.apihandlers.SampleApis$MemInfoApi"
        memAllocate = "com.github.btnguyen2k.mus.apihandlers.SampleApis$MemAllocateApi"
        memClear    = "com.github.btnguyen2k.mus.apihandlers.SampleApis$MemClearApi"
        memFree     = "com.github.btnguyen2k.mus.apihandlers.SampleApis$MemFreeApi"
    }
}
```

Valid API handlers are registered with a global `com.github.ddth.recipes.apiservice.ApiRouter` instance.

RESTful API endpoints are configured via config-key `api.endpoints`:

```
api {
    # API endpoints
    endpoints {
        "/api/samples/memInfo" {
            get = "memInfo"         # GET /api/samples/memInfo will be handled by API handler name "memInfo"
        }
        "/api/samples/memAllocate" {
            post = "memAllocate"    # POST /api/samples/memAllocate will be handled by API handler name "memAllocate"
            put  = "memAllocate"    
        }
        "/api/samples/memClear" {
            post = "memClear"
            put  = "memClear"       # PUT /api/samples/memClear will be handled by API handler name "memClear"
        }
        "/api/samples/memFree" {
            post = "memFree"
            put  = "memFree"
        }
    }
}
```

**Define and register APIs via annotation**

Use annotation `io.swagger.v3.oas.annotations.Operation` to declare API handler and endpoint at the same time:

```java
public class ListAllCategories implements IApiHandler {
    @Operation(operationId = "listCategories", summary = "Return all categories.", method = "get:/api/petstore/categories", tags = { "petstore" })
    @Override
    public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
        //...
    }
}
```

- `operationId` is used to name the API handler.
- `method` is used to define API endpoints.

Declare API's in-path parameters:

```java
public class DeleteCategory implements IApiHandler {
    @Operation(operationId = "deleteCategory", summary = "Delete an existing category.", method = "delete:/api/petstore/category/{id}", tags = { "petstore" },
        parameters = {
            @Parameter(name = "id", description = "Category's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string"))
        })
    @Override
    public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
        //...
    }
}
```

- Use `io.swagger.v3.oas.annotations.enums.ParameterIn.PATH` to declare an in-path parameter.

Most of the time, API parameters are encoded inside request's body as JSON object:

```java
public class UpdatePet implements IApiHandler {
    @Operation(operationId = "updatePet", summary = "Update an existing pet.", method = "put:/api/petstore/pet/{id}", tags = { "petstore" },
        parameters = {
            @Parameter(name = "id", description = "Pet's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")),
            @Parameter(name = "name", description = "Pet's name", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")),
            @Parameter(name = "category", description = "Pet's category-id", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")),
            @Parameter(name = "status", description = "Pet's status", in = ParameterIn.DEFAULT, schema = @Schema(type = "string", allowableValues = { "AVAILABLE", "PENDING", "SOLD" }))
        })
    @Override
    public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
        //...
    }
}
``` 

- Parameters with `in=ParameterIn.DEFAULT` are in-request-body parameters.

**Swagger Spec file and Swagger UI**

Swagger Spec file in JSON format is dynamically generated at URI `/swagger.json`.
Also an embedded Swagger UI can be access via `/swagger-ui`.

## Additional Bootstrapping Routines

Additional bootstrappers are defined in configuration file, at key `bootstrappers` - which is an array of strings specifying bootstrapper classes.
- Bootstrappers are called in order of appearance.
- Bootstrapper must implement `java.lang.Runnable`.
- If a bootstrapper throws exception, it is logged and next bootstrapper is called.

Example:

```
## Bootstrap configurations
bootstrappers = [
    # List of bootstrappers, will be called in order of occurrence
    # Bootstrapper must implement java.lang.Runnable

    "com.github.btnguyen2k.mus.bootstrappers.SpringApplicationContextBootstrapper"
]
```

The template include a built-in bootstrapper `SpringApplicationContextBootstrapper` that creates and initializes a Spring's `ApplicationContext` instance.

## Start/Stop Scripts

- Start: `sh conf/server-prod.sh start` or `sh conf/server-docker.sh stop`
- Stop : `sh conf/server-prod.sh stop` or `sh conf/server-docker.sh stop`

Command-line arguments:
- `-h`: display help screen and exist.
- `--pid <path-to.pid-file>`: specify `.pid` file that store the process id when the application is running (default `./<app-name>.pid`).
- `-m <max-memory-in-mb>` or `--mem <max-memory-in-mb>`: maximum amount of memory used for heap in mb.
- `-c <path-to-config-file.conf>` or `--conf <path-to-config-file.conf>`: specify application's configuration file (default `./conf/application.conf`).
- `-l <path-to-logback-file.xml>` or `--log <path-to-logback-file.xml>`: specify the logback configuration file
  - default `./conf/logback-dev.xml` for development environment.
  - default `./conf/logback-prod.xml` for production environment.
  - default `./conf/logback-docker.xml` for container environment.
- `-j <extra-jvm-options>` or `--jvm <extra-jvm-options>`: extra JVM options (e.g. `-j "-Djava.rmi.server.hostname=localhost"`)

## Docker support

Application can be packaged as a Docker image, in two ways:

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
docker build -t $name$:$app_version$ ./target/docker/stage
```

See more: http://www.scala-sbt.org/sbt-native-packager/formats/docker.html

## LICENSE & COPYRIGHT

See [LICENSE.md](LICENSE.md) for details. Copyright (c) 2018-2019 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.

## Giter8 template

For information on giter8 templates, please see http://www.foundweekends.org/giter8/

# $name$

**$desc$** by **$app_author$ - $organization$**, based on [microservices-undertow-seed.g8](https://github.com/btnguyen2k/microservices-undertow-seed.g8).

Copyright (C) by **$organization$**.
F
Latest release version: `$app_version$`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

## Usage Summary

- Open project in IDE:
  - Generate Eclipse project with `sbt eclipse`.
  - IntelliJ: simply open project in IntelliJ.
- Build:
  - Build standalone `.zip` distribution package: `sbt universal:packageBin`
  - Build Docker package (Dockerfile and application binary): `sbt docker:stage`
  - Build Docker image and publish to local in one-go: `sbt docker:publishLocal`

## Configurations

Application's main configuration file `conf/application.conf` in [HOCON format](https://github.com/lightbend/config/blob/master/HOCON.md).

### Application info

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

### Additional Bootstrappers

```
## Bootstrap configurations
bootstrappers = [
    # List of bootstrappers, will be called in order of occurrence
    # Bootstrapper must implement java.lang.Runnable

    "com.github.btnguyen2k.mus.bootstrappers.SpringApplicationContextBootstrapper"
]
```

There is a built-in bootstrapper `SpringApplicationContextBootstrapper` creates and initializes a Spring's `ApplicationContext` instance.

### API handlers

```
api {
    handlers {
        #handlerName = "Java class name", must implement com.github.ddth.recipes.apiservice.IApiHandler
        memInfo     = "com.github.btnguyen2k.mus.apihandlers.SampleApis\$MemInfoApi"
        memAllocate = "com.github.btnguyen2k.mus.apihandlers.SampleApis\$MemAllocateApi"
        memClear    = "com.github.btnguyen2k.mus.apihandlers.SampleApis\$MemClearApi"
        memFree     = "com.github.btnguyen2k.mus.apihandlers.SampleApis\$MemFreeApi"
    }
}
```

### RESTful API endpoints

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

## Define and register APIs via annotation

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

## Build & Package application

### Standalone application

Standalone application (in `.zip` format)can be built via command `sbt universal:packageBin`. The generated `.zip` file will be created under directory `target/universal`.

### Dockerized aplication

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

See [LICENSE.md](LICENSE.md).

Third party libraries are distributed under their own licenses.

## Giter8 template

For information on giter8 templates, please see http://www.foundweekends.org/giter8/

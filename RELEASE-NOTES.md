# microservices-undertow-seed.g8 Release Notes

## 2019-09-21: template-v2.0.r6

- Migrate to `sbt-v1.13.2` and `scala-v2.13.1`
- `server-docker.sh` & `server-prod.sh`:
  - Remove deprecated JVM options.
  - Enable `Graal JIT`
  - Remove [GC log rotation](https://dzone.com/articles/try-to-avoid-xxusegclogfilerotation).
- Other fixes and enhancements.


## 2019-09-19: template-v2.0.r5

- Fixed bug: request body is sometimes truncated.
- Other fixes and enhancements.


## 2019-09-08: template-v2.0.r4

- API performance log:
  - Log to console
  - Log to InfluxDB
- API request log:
  - Log to console
  - Log to ElasticSearch


## 2019-09-01: template-v2.0.r3

- Undertow `2.0.25.Final`
- Upgrade dependency libs.
- Swagger support:
  - Auto generate Swagger API spec file in JSON format.
  - Bundle Swagger-UI
  - Annotate API with `io.swagger.v3.oas.annotations.Operation`.
- WebJars support.
- APIs can now be defined in application configuration files or via `io.swagger.v3.oas.annotations.Operation` annotation.
- Add scaffold `petstore`.
- Additional bootstrappers via configuration `bootstrappers`:
  - Builtin `SpringApplicationContextBootstrapper`
- JVM tuning:
  - `server-prod.sh`: `-J-XX:+UseParallelGC -J-XX:MinHeapFreeRatio=5 -J-XX:MaxHeapFreeRatio=10 -J-XX:-ShrinkHeapInSteps -J-XX:MaxGCPauseMillis=100`
  - `server-docker.sh`: `-J-XX:+UseSerialGC -J-XX:MinHeapFreeRatio=5 -J-XX:MaxHeapFreeRatio=10 -J-XX:-ShrinkHeapInSteps -J-XX:MaxGCPauseMillis=100`

**Breaking changes:**
- **Migrate to `Java11`**.
- Configuration changes:
  - `api.routes` is replaced by `api.endpoints`


## 2019-03-15: template-v2.0.r2

- Undertow `2.0.19.Final`.
- Bug fix: sub-directories under `./conf/` are not copied.
- Add class `ParseApiAuthHttpHandler` that extracts API authentication info from HTTP request, attaches to the exchange and passes to next HTTP handler.
- Upgrade dependency libs.


## 2018-10-12: template-v2.0.r1

First release:

- Undertow `2.0.13.Final`.
- JSON-encoded REST API framework.
- API samples: `/api/samples/echo` & `/api/samples/info`

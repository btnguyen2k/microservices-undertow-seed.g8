# Release Notes

## 2018-01-16: template-v0.1.0

First release:

- Build and package project with `sbt`:
  - Support build standalone package
  - Support build Docker image
  - Support generate Eclipse project
- Support multiple Gearman servers
- Support multiple functions
- 2 types of job handlers:
  - `RunAllJobHandler`: accept and run all incoming jobs
  - `RunIfNotBusyJobHandler`: accept and run incoming jobs if not busy
- Samples job handler implementations in package `com.github.btnguyen2k.gearmanworker.samples`

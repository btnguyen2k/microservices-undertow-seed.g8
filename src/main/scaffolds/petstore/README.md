# Sample APIs: pet store

The famous "petstore" example.

Usage:
- Generate files with command `sbt "g8Scaffold petstore"`
- Copy file `.g8/petstore/conf/api_scaffolds.conf` to `conf/sapi_scaffolds.conf`, override the existing file: `cp .g8/petstore/conf/api_scaffolds.conf conf/api_scaffolds.conf`

Command in one go:

```shell
sbt "g8Scaffold petstore" && cp .g8/petstore/conf/api_scaffolds.conf conf/api_scaffolds.conf
```

# microservices-undertow-seed.g8 Release Notes

## 2019-03-15: template-v2.0.r2

- Undertow `2.0.19.Final`.
- Bug fix: sub-directories under `./conf/` are not copied.
- Add class `ParseApiAuthHttpHandler` that extracts API authentication info from HTTP request, attaches to the exchange and passes to next HTTP handler.
- Upgrade dependency libs.


## 2018-10-12: template-v2.0.r2

First release:

- Undertow `2.0.13.Final`.
- JSON-encoded REST API framework.
- API samples: `/api/samples/echo` & `/api/samples/info`

# eureka-discovery

The service registry: a standalone Eureka server. `user-service`, `auth-service`, `api-gateway` and the Config Server register with it, and the gateway resolves `lb://` routes through it.

Part of the identity platform; the platform root is [`../micro-services`](../micro-services/README.md). This repository must sit next to it, because the version catalog is read from there.

## Run

The usual way is the whole stack: `make up` in `../micro-services`. Dashboard at http://localhost:9111.

From source: `./gradlew bootRun`.

## Test

```bash
./gradlew build
```

## Configuration

| Variable | Default | Meaning |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev`, `qa` or `prod` |
| `SERVER_PORT` | `9111` | HTTP port |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` in `dev`, required otherwise | Name the registry advertises |

Configuration is split by environment: `application.yml` plus `application-dev.yml`, `-qa.yml` and `-prod.yml`. Self-preservation is off in `dev`, where there is a single instance, and on in `qa` and `prod`. It runs without authentication inside the network.

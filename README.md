# eureka-discovery

The service registry of the identity platform: a standalone Netflix Eureka server. Services register here when they start, and the gateway looks them up here to route `lb://user-service` and `lb://auth-service`.

It has no API of its own beyond Eureka's, no database and no secrets.

## Who uses it

| Client | Registers | Looks others up |
| --- | --- | --- |
| `user-service` | yes | no |
| `auth-service` | yes | no |
| `api-gateway` | yes | yes, to route requests |
| `cloud-config-service` | yes | no |

Application name: `eureka-discovery-service`. Port: 9111.

## Endpoints

| Path | What |
| --- | --- |
| `/` | Dashboard: registered instances and their status |
| `/eureka/apps` | The registry (Eureka's REST API; send `Accept: application/json`) |
| `/eureka/apps/{APP}` | The instances of one application |
| `/actuator/health`, `/actuator/health/readiness` | Health |

```bash
curl -s -H 'Accept: application/json' localhost:9111/eureka/apps | jq '[.applications.application[].name]'
```

## Behaviour

- **Standalone:** it neither registers with nor fetches from a peer.
- **Self-preservation** is off in `dev`, where there is one instance and a stopped service should disappear quickly, and on in `qa` and `prod`, where registrations should survive a network blip.
- **Clients** renew every 10 seconds and register by IP address (set in `service-configs/application.yml`).
- A service deregisters itself when it is stopped gracefully, which is why the stop script stops services before the registry.
- It runs without authentication inside the platform network.

## Configuration

Split by environment ([ADR 0014](../micro-services/docs/adr/0014-environment-profiles.md)):

| File | Holds |
| --- | --- |
| `application.yml` | Name, port, standalone settings, graceful shutdown, health endpoints |
| `application-dev.yml` | Host name defaulting to `localhost`; self-preservation off |
| `application-qa.yml`, `application-prod.yml` | Host name required; self-preservation on |

| Variable | Default in `dev` | Meaning |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev`, `qa` or `prod` |
| `SERVER_PORT` | `9111` | HTTP port |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | The name the registry advertises; `eureka-discovery` inside Compose and Kubernetes |

It does not use the Config Server: the Config Server registers with it, so it must be able to start first.

## Run

This repository must sit next to [`micro-services`](../micro-services/README.md), which holds the version catalog.

**With the whole platform** (the usual way): `cd ../micro-services && make up`. It starts in stage 4 of `scripts/start.sh`, after Keycloak and the bootstrap and before the services. Dashboard at http://localhost:9111.

**On its own, from source:** `./gradlew bootRun`

**Restart it:** `cd ../micro-services && scripts/restart.sh eureka-discovery`. Services re-register within about 10 seconds.

## Test

```bash
./gradlew build
```

3 tests, none skipped, no Docker needed: the registry starts empty, accepts a registration and lists the instance, and reports readiness.

## Build and image

- Java 27, Gradle 9.8.0 (wrapper), Spring Boot 4.1.1, Spring Cloud 2025.1.3. Versions come from `../micro-services/gradle/libs.versions.toml`.
- `Dockerfile` is multi-stage: build on JDK 27, run on a JRE 27 Alpine image as a non-root user, with a health check on `/actuator/health/readiness`. It needs the platform root as a named build context:

```bash
docker build --build-context platform=../micro-services -t eureka-discovery .
```

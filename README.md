# Eureka Discovery

Eureka Discovery provides the service registry used by discovery-enabled microservices and the gateway. It stores instance location/health metadata, not domain data, and has no application database dependency.

The repository is named `eureka-discovery`; the application/image name is `eureka-discovery-service`. Preserve that distinction when building images or addressing Compose services.

## Current Technology

Java 21, Spring Boot 3.4.7, Spring Cloud 2024.0.0, and Gradle 8.14.3 using its independent Groovy-DSL wrapper. The application uses `@EnableEurekaServer`.

The Docker runtime is Java 27, but the build/framework migration to Java 27 and a compatible current Spring Cloud release remains pending.

## Architecture And Relationships

Discovery is an infrastructure capability, not a business bounded context requiring aggregates. Clients register under their Spring application name. Gateway `lb://` routes require matching registered service IDs and discovery enabled on the gateway.

Current default HTTP port is 9111. Hostname defaults to localhost; instances prefer IP addresses. Server registration is disabled. `fetch-registry` still needs explicit standalone-server hardening.

## Endpoints

| URL | Purpose |
| --- | --- |
| `http://localhost:9111/` | Eureka dashboard |
| `http://localhost:9111/eureka/` | Client registration/discovery base |
| `http://localhost:9111/eureka/apps` | Registry listing |

```bash
curl -H 'Accept: application/json' http://localhost:9111/eureka/apps
```

The current build does not include an Actuator starter. Do not assume `/actuator/health` works simply because a Compose health check references it.

## Build And Run

Use Java 21 for the current build and run from this repository root:

```bash
bash ./gradlew clean test bootJar
bash ./gradlew bootRun
```

The executable JAR is `build/libs/eureka-discovery-service-1.0.jar`.

The standalone Compose recipe publishes 9111:

```bash
docker compose config --quiet
docker compose up -d --build
```

A leftover database volume definition is not an active database requirement.

## Consumer Configuration

A host-based client's registry URL is `http://localhost:9111/eureka`; within shared Compose it is `http://eureka-discovery-service:9111/eureka`. Enable each client's discovery integration explicitly where its local defaults disable it.

No live registry security is currently configured. Keep the registry private to trusted infrastructure until authenticated access and production network restrictions are implemented.

## Troubleshooting And Pending Work

If the dashboard works but a service is absent, check client dependencies, enabled flags, application names, registration URLs, and connectivity from the client container. A gateway 503 can also result from discovery being disabled even when Eureka itself is healthy.

Remaining work: Java 27/Boot 4 migration, compatible Spring Cloud alignment, standalone registry settings, health-check/Actuator alignment, security, and collective startup verification.

See [gateway documentation](../api-gateway/README.md), [platform orchestration](../micro-services/README.md), and the [implementation checkpoint](../micro-services/IAM_IMPLEMENTATION_CHECKPOINT.md).

## Repository Map

```text
eureka-discovery/
  build.gradle                         Spring Boot 3.4.7 / Spring Cloud 2024.0.0 build
  Dockerfile                           Layered JRE image recipe
  compose.yml                          Standalone local registry recipe
  src/main/java/discovery/DiscoveryServer.java
                                       @SpringBootApplication + @EnableEurekaServer
  src/main/resources/application.yaml  Registry port, hostname, and client self-registration settings
```

## Registry Contract

| Property | Default | Meaning |
| --- | --- | --- |
| `SPRING_ACTIVE_PROFILE` | `dev` | Active profile. Dev and QA currently share the same registry settings. |
| `SERVER_PORT` | `9111` | Registry dashboard and API port. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname used to form the local default zone. |
| `EUREKA_INSTANCE_PREFER_IPADDRESS` | `true` | Client metadata favors IP addresses. |

Host clients use:

```text
http://localhost:9111/eureka
```

Shared Compose clients use:

```text
http://eureka-discovery-service:9111/eureka
```

Service names must match each client's `spring.application.name`. For example, a gateway `lb://user-service` route resolves only when an instance registers as `user-service`.

## Command Reference

| Task | Command |
| --- | --- |
| Test | `bash ./gradlew test` |
| Build JAR | `bash ./gradlew bootJar` |
| Full build | `bash ./gradlew clean test bootJar` |
| Run on host | `bash ./gradlew bootRun` |
| Build image | `docker build -t eureka-discovery-service:latest .` |
| Start standalone Compose | `docker compose up -d --build` |
| List registered apps | `curl -H 'Accept: application/json' http://localhost:9111/eureka/apps` |

## Operational Notes

- This build does not include Actuator; use the dashboard or `/eureka/apps` for basic checks.
- The registry is currently unauthenticated. Keep it on a trusted network until security is added.
- A healthy registry does not imply clients have discovery enabled. Check each service's `EUREKA_ENABLED` or corresponding Spring Cloud Eureka flags.
- The Dockerfile expects `build/libs/eureka-discovery-service-1.0.jar` to exist before image build.

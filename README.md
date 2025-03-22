# Eureka-Discovery

Spring Cloud Eureka Discovery Server i.e. service discovery


### Run

---

The following command builds an image and tags it as srikanthkakumanu/eureka-discovery and runs the Docker image locally. The build creates a spring user and spring group to run the application.

``````bash

docker build --build-arg JAR_FILE=build/libs/eureka-discovery-1.0.jar -t srikanthkakumanu/eureka-discovery .
``````

Run the application with user privileges helps to mitigate some risks. So, an important improvement to the Dockerfile is to run the application as a non-root user.

### Build Docker Image

---

```bash

./gradlew bootBuildImage --imageName=srikanthkakumanu/eureka-discovery
```

or

```bash
docker build -t srikanthkakumanu/eureka-discovery:1.0 .
```

### Push Docker Image to DockerHub

---

```bash

docker image push srikanthkakumanu/eureka-discovery:1.0
```

### Using Spring Profiles

---

```bash

docker run -e "SPRING_PROFILES_ACTIVE=prod" -p 8080:8080 -t srikanthkakumanu/eureka-discovery
```

or

```bash
docker run -e "SPRING_PROFILES_ACTIVE=dev" -p 8080:8080 -t srikanthkakumanu/eureka-discovery
```

### Debug App in Docker container (using JPDA)

---

```bash
docker run -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" -p 8080:8080 -p 5005:5005 -t srikanthkakumanu/eureka-discovery
```

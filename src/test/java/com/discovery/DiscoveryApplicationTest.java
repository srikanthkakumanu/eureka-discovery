package com.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class DiscoveryApplicationTest {

	@Value("${local.server.port}")
	private int port;

	@Test
	void servesAnEmptyRegistry() {
		String apps = client().get().uri("/eureka/apps").accept(MediaType.APPLICATION_JSON).retrieve()
				.body(String.class);

		assertThat(apps).contains("\"applications\"").contains("\"application\":[]");
	}

	@Test
	void acceptsARegistrationAndListsTheInstance() {
		String instance = """
				{"instance": {"instanceId": "sample:1", "hostName": "sample-host", "app": "SAMPLE-SERVICE",
				 "ipAddr": "10.0.0.1", "vipAddress": "sample-service", "status": "UP",
				 "port": {"$": 8080, "@enabled": "true"},
				 "dataCenterInfo": {"@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo", "name": "MyOwn"}}}
				""";

		var registered = client().post().uri("/eureka/apps/SAMPLE-SERVICE").contentType(MediaType.APPLICATION_JSON)
				.body(instance).retrieve().toBodilessEntity();
		String listed = client().get().uri("/eureka/apps/SAMPLE-SERVICE").accept(MediaType.APPLICATION_JSON)
				.retrieve().body(String.class);

		assertThat(registered.getStatusCode().value()).isEqualTo(204);
		assertThat(listed).contains("sample:1").contains("sample-host");
	}

	@Test
	void reportsReadiness() {
		String health = client().get().uri("/actuator/health/readiness").retrieve().body(String.class);

		assertThat(health).contains("\"status\":\"UP\"");
	}

	private RestClient client() {
		return RestClient.create("http://localhost:" + port);
	}
}

package com.clinica.mariana.restms.users.service;

import com.clinica.mariana.restms.auth.properties.KeycloakProperties;
import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;
import com.clinica.mariana.restms.users.dto.UserSummaryDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void shouldListUsersFromKeycloak() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/realms/rest-ms/protocol/openid-connect/token", exchange -> {
			assertThat(exchange.getRequestMethod()).isEqualTo("POST");
			String body = readBody(exchange);
			assertThat(body).contains("grant_type=password");
			assertThat(body).contains("username=api-admin");
			writeJson(exchange, 200, """
					{"access_token":"admin-token"}
					""");
		});
		server.createContext("/admin/realms/rest-ms/users", exchange -> {
			assertThat(exchange.getRequestMethod()).isEqualTo("GET");
			assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer admin-token");
			writeJson(exchange, 200, """
					[
					  {
					    "id": "u-1",
					    "username": "maria.silva",
					    "email": "maria.silva@clinic.local",
					    "enabled": true,
					    "firstName": "Maria",
					    "lastName": "Silva"
					  }
					]
					""");
		});
		server.start();

		UserService service = new UserService(keycloakProperties());

		List<UserSummaryDto> users = service.listUsers();

		assertThat(users).containsExactly(
				new UserSummaryDto("u-1", "maria.silva", "maria.silva@clinic.local", true, "Maria", "Silva"));
	}

	@Test
	void shouldCreateUserAndAssignRole() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/realms/rest-ms/protocol/openid-connect/token", exchange -> writeJson(exchange, 200, """
				{"access_token":"admin-token"}
				"""));
		server.createContext("/admin/realms/rest-ms/users", exchange -> {
			if ("POST".equals(exchange.getRequestMethod())) {
				assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer admin-token");
				String body = readBody(exchange);
				assertThat(body).contains("\"username\":\"maria.silva\"");
				assertThat(body).contains("\"email\":\"maria.silva@clinic.local\"");
				assertThat(body).contains("\"firstName\":\"Maria\"");
				assertThat(body).contains("\"lastName\":\"Silva\"");
				exchange.getResponseHeaders().add("Location", "http://localhost:%s/admin/realms/rest-ms/users/user-123"
						.formatted(server.getAddress().getPort()));
				exchange.sendResponseHeaders(201, -1);
				exchange.close();
				return;
			}
			writeJson(exchange, 405, """
					{}
					""");
		});
		server.createContext("/admin/realms/rest-ms/roles/DOCTOR", exchange -> {
			assertThat(exchange.getRequestMethod()).isEqualTo("GET");
			assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer admin-token");
			writeJson(exchange, 200, """
					{"id":"role-123","name":"DOCTOR"}
					""");
		});
		server.createContext("/admin/realms/rest-ms/users/user-123/role-mappings/realm", exchange -> {
			assertThat(exchange.getRequestMethod()).isEqualTo("POST");
			assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer admin-token");
			String body = readBody(exchange);
			assertThat(body).contains("\"id\":\"role-123\"");
			assertThat(body).contains("\"name\":\"DOCTOR\"");
			exchange.sendResponseHeaders(204, -1);
			exchange.close();
		});
		server.start();

		UserService service = new UserService(keycloakProperties());

		CreateUserResponseDto response = service.createUser(new CreateUserRequestDto("maria.silva",
				"maria.silva@clinic.local", "Maria", "Silva", "SenhaForte123", "DOCTOR"));

		assertThat(response)
				.isEqualTo(new CreateUserResponseDto("user-123", "maria.silva", "maria.silva@clinic.local", "DOCTOR"));
	}

	private KeycloakProperties keycloakProperties() {
		return new KeycloakProperties("http://localhost:%s".formatted(server.getAddress().getPort()), "rest-ms",
				"rest-ms-api", "rest-ms-api-secret", "api-admin", "api-admin123");
	}

	private String readBody(HttpExchange exchange) throws IOException {
		return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	}

	private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
		byte[] content = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, content.length);
		exchange.getResponseBody().write(content);
		exchange.close();
	}
}

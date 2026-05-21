package com.clinica.mariana.restms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatasourceUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String PROPERTY_SOURCE_NAME = "normalizedDatasourceUrl";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String datasourceUrl = environment.getProperty("spring.datasource.url");
		if (!StringUtils.hasText(datasourceUrl)) {
			datasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
		}

		NormalizedDatasourceUrl normalized = normalize(datasourceUrl);
		if (normalized == null) {
			return;
		}

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("spring.datasource.url", normalized.jdbcUrl());
		if (StringUtils.hasText(normalized.username())) {
			properties.put("spring.datasource.username", normalized.username());
		}
		if (StringUtils.hasText(normalized.password())) {
			properties.put("spring.datasource.password", normalized.password());
		}

		environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	private NormalizedDatasourceUrl normalize(String datasourceUrl) {
		if (!StringUtils.hasText(datasourceUrl) || datasourceUrl.startsWith("jdbc:postgresql://")) {
			return null;
		}
		if (!datasourceUrl.startsWith("postgresql://") && !datasourceUrl.startsWith("postgres://")) {
			return null;
		}

		URI uri = URI.create(datasourceUrl);
		String userInfo = uri.getRawUserInfo();
		String username = null;
		String password = null;
		if (StringUtils.hasText(userInfo)) {
			String[] parts = userInfo.split(":", 2);
			username = decode(parts[0]);
			if (parts.length > 1) {
				password = decode(parts[1]);
			}
		}

		StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
				.append(uri.getHost());
		if (uri.getPort() > -1) {
			jdbcUrl.append(':').append(uri.getPort());
		}
		if (StringUtils.hasText(uri.getRawPath())) {
			jdbcUrl.append(uri.getRawPath());
		}

		Map<String, String> queryParams = parseQuery(uri.getRawQuery());
		if (StringUtils.hasText(username)) {
			queryParams.putIfAbsent("user", username);
		}
		if (StringUtils.hasText(password)) {
			queryParams.putIfAbsent("password", password);
		}
		if (!queryParams.isEmpty()) {
			jdbcUrl.append('?').append(toQuery(queryParams));
		}

		return new NormalizedDatasourceUrl(jdbcUrl.toString(), username, password);
	}

	private Map<String, String> parseQuery(String query) {
		Map<String, String> queryParams = new LinkedHashMap<>();
		if (!StringUtils.hasText(query)) {
			return queryParams;
		}
		for (String param : query.split("&")) {
			String[] parts = param.split("=", 2);
			String key = decode(parts[0]);
			String value = parts.length > 1 ? decode(parts[1]) : "";
			if (StringUtils.hasText(key)) {
				queryParams.put(key, value);
			}
		}
		return queryParams;
	}

	private String toQuery(Map<String, String> queryParams) {
		return queryParams.entrySet().stream()
				.map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
				.reduce((left, right) -> left + "&" + right)
				.orElse("");
	}

	private String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private record NormalizedDatasourceUrl(String jdbcUrl, String username, String password) {
	}
}

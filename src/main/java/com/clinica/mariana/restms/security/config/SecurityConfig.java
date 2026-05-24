package com.clinica.mariana.restms.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final RestAccessDeniedHandler restAccessDeniedHandler;

	public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint,
			RestAccessDeniedHandler restAccessDeniedHandler) {
		this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
		this.restAccessDeniedHandler = restAccessDeniedHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers("/actuator/health", "/swagger-ui/**", "/swagger-ui.html",
										"/v3/api-docs.yaml", "/v3/api-docs", "/v3/api-docs/**", "/auth/login",
										"/api/v1/auth/login", "/error", "/api/v1/error")
								.permitAll().anyRequest().authenticated())
				.exceptionHandling(
						exceptionHandling -> exceptionHandling.authenticationEntryPoint(restAuthenticationEntryPoint)
								.accessDeniedHandler(restAccessDeniedHandler))
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
		return jwt -> new JwtAuthenticationToken(jwt, extractAuthorities(jwt),
				jwt.getClaimAsString("preferred_username"));
	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		Object realmAccess = jwt.getClaims().get("realm_access");
		if (realmAccess instanceof Map<?, ?> realmAccessMap) {
			Object roles = realmAccessMap.get("roles");
			if (roles instanceof Collection<?> roleList) {
				for (Object role : roleList) {
					if (role instanceof String roleName && !roleName.isBlank()) {
						authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
					}
				}
			}
		}
		return authorities;
	}
}

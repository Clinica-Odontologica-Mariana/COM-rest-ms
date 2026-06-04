package com.clinica.mariana.restms.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		request.setAttribute("startedAtMs", System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		Long startedAt = (Long) request.getAttribute("startedAtMs");
		long elapsed = startedAt == null ? -1 : (System.currentTimeMillis() - startedAt);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String user = authentication == null ? "anonymous" : authentication.getName();

		LOGGER.info("method={} path={} status={} user={} elapsedMs={}", request.getMethod(), request.getRequestURI(),
				response.getStatus(), user, elapsed);
	}
}

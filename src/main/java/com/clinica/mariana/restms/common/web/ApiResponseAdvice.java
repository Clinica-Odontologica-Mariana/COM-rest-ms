package com.clinica.mariana.restms.common.web;

import com.clinica.mariana.restms.common.api.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return !StringHttpMessageConverter.class.isAssignableFrom(converterType)
				&& !ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)
				&& !ResourceHttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		String path = request.getURI().getPath();
		if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/api/v1/v3/api-docs")
				|| path.startsWith("/api/v1/swagger-ui")) {
			return body;
		}
		if (body instanceof ApiResponse<?> || body instanceof byte[]) {
			return body;
		}
		return ApiResponse.success(body);
	}
}

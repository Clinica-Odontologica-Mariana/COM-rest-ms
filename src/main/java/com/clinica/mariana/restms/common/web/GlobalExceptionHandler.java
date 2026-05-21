package com.clinica.mariana.restms.common.web;

import com.clinica.mariana.restms.common.api.ApiError;
import com.clinica.mariana.restms.common.api.ApiResponse;
import com.clinica.mariana.restms.common.exception.AppException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex, WebRequest request) {
		return buildErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), ex.getDetails(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors()
				.stream()
				.map(this::toFieldMessage)
				.toList();
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		List<String> details = ex.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details, request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Object>> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
		String code = "HTTP_" + ex.getStatusCode().value();
		String reason = ex.getReason() == null ? "Request failed" : ex.getReason();
		return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), code, reason, List.of(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", List.of(), request);
	}

	private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
			HttpStatus status,
			String code,
			String message,
			List<String> details,
			WebRequest request
	) {
		ApiError error = new ApiError(
				code,
				message,
				details,
				Instant.now(),
				extractPath(request)
		);
		return ResponseEntity.status(status).body(ApiResponse.failure(error));
	}

	private String extractPath(WebRequest request) {
		if (request instanceof ServletWebRequest servletWebRequest) {
			return servletWebRequest.getRequest().getRequestURI();
		}
		return "";
	}

	private String toFieldMessage(FieldError fieldError) {
		return fieldError.getField() + ": " + fieldError.getDefaultMessage();
	}
}

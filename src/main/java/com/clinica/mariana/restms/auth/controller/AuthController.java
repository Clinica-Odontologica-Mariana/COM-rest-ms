package com.clinica.mariana.restms.auth.controller;

import com.clinica.mariana.restms.auth.dto.LoginRequestDto;
import com.clinica.mariana.restms.auth.dto.LoginResponseDto;
import com.clinica.mariana.restms.auth.service.AuthService;
import com.clinica.mariana.restms.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public AuthenticatedUser me(@AuthenticationPrincipal Jwt jwt) {
		return AuthenticatedUser.fromJwt(jwt);
	}
}

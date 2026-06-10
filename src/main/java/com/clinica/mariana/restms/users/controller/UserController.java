package com.clinica.mariana.restms.users.controller;

import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;
import com.clinica.mariana.restms.users.dto.UserSummaryDto;
import com.clinica.mariana.restms.users.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@RolesAllowed("ADMIN")
	public List<UserSummaryDto> listUsers() {
		return userService.listUsers();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed("ADMIN")
	public CreateUserResponseDto createUser(@Valid @RequestBody CreateUserRequestDto request) {
		return userService.createUser(request);
	}
}

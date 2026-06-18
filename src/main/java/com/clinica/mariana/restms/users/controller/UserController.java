package com.clinica.mariana.restms.users.controller;

import com.clinica.mariana.restms.security.model.AuthenticatedUser;
import com.clinica.mariana.restms.users.dto.ChangePasswordDto;
import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;
import com.clinica.mariana.restms.users.dto.UpdateProfileDto;
import com.clinica.mariana.restms.users.dto.UpdateUserDto;
import com.clinica.mariana.restms.users.dto.UserProfileDto;
import com.clinica.mariana.restms.users.dto.UserSummaryDto;
import com.clinica.mariana.restms.users.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<UserSummaryDto> listUsers() {
		return userService.listUsers();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN"})
	public CreateUserResponseDto createUser(@Valid @RequestBody CreateUserRequestDto request) {
		return userService.createUser(request);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN"})
	public void updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserDto request) {
		userService.updateUser(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN"})
	public void deleteUser(@PathVariable String id) {
		userService.deleteUser(id);
	}

	@GetMapping("/me")
	public UserProfileDto getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
		AuthenticatedUser user = AuthenticatedUser.fromJwt(jwt);
		return userService.getCurrentUser(user.subject(), user.username(), user.roles());
	}

	@PatchMapping("/me")
	public UserProfileDto updateCurrentUser(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateProfileDto request) {
		AuthenticatedUser user = AuthenticatedUser.fromJwt(jwt);
		return userService.updateCurrentUser(user.subject(), user.username(), user.roles(), request);
	}

	@PostMapping("/me/change-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordDto request) {
		userService.changePassword(jwt.getSubject(), jwt.getClaimAsString("preferred_username"), request);
	}
}

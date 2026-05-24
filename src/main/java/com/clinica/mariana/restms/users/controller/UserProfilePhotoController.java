package com.clinica.mariana.restms.users.controller;

import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.storedfile.dto.UserProfilePhotoDto;
import com.clinica.mariana.restms.storedfile.service.UserProfilePhotoService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserProfilePhotoController {

	private final UserProfilePhotoService userProfilePhotoService;

	public UserProfilePhotoController(UserProfilePhotoService userProfilePhotoService) {
		this.userProfilePhotoService = userProfilePhotoService;
	}

	@PostMapping("/me/profile-photo")
	@ResponseStatus(HttpStatus.CREATED)
	public UserProfilePhotoDto uploadOwnPhoto(@AuthenticationPrincipal Jwt jwt, @RequestParam MultipartFile file) {
		return userProfilePhotoService.uploadOwnPhoto(jwt.getSubject(), file);
	}

	@GetMapping("/me/profile-photo")
	public UserProfilePhotoDto findOwnPhoto(@AuthenticationPrincipal Jwt jwt) {
		return userProfilePhotoService.findOwnPhoto(jwt.getSubject());
	}

	@GetMapping("/me/profile-photo/download-url")
	public PresignedUrlDto ownDownloadUrl(@AuthenticationPrincipal Jwt jwt) {
		return userProfilePhotoService.ownDownloadUrl(jwt.getSubject());
	}

	@DeleteMapping("/me/profile-photo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOwnPhoto(@AuthenticationPrincipal Jwt jwt) {
		userProfilePhotoService.deleteOwnPhoto(jwt.getSubject());
	}

	@PostMapping("/{userId}/profile-photo")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed("ADMIN")
	public UserProfilePhotoDto uploadUserPhoto(@PathVariable UUID userId, @RequestParam MultipartFile file) {
		return userProfilePhotoService.uploadForUser(userId, null, file);
	}

	@GetMapping("/{userId}/profile-photo")
	@RolesAllowed("ADMIN")
	public UserProfilePhotoDto findUserPhoto(@PathVariable UUID userId) {
		return userProfilePhotoService.findForUser(userId);
	}

	@GetMapping("/{userId}/profile-photo/download-url")
	@RolesAllowed("ADMIN")
	public PresignedUrlDto userDownloadUrl(@PathVariable UUID userId) {
		return userProfilePhotoService.downloadUrlForUser(userId);
	}

	@DeleteMapping("/{userId}/profile-photo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void deleteUserPhoto(@PathVariable UUID userId) {
		userProfilePhotoService.deleteForUser(userId);
	}
}

package com.clinica.mariana.restms.users.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.users.dto.UserProfilePhotoDto;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.users.entity.UserProfilePhotoEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.service.StoredFileService;
import com.clinica.mariana.restms.users.repository.UserProfilePhotoRepository;
import com.clinica.mariana.restms.users.repository.AppUserReferenceRepository;
import com.clinica.mariana.restms.users.repository.AppUserReferenceRepository.AppUserReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UserProfilePhotoService {

	private final UserProfilePhotoRepository userProfilePhotoRepository;
	private final AppUserReferenceRepository appUserReferenceRepository;
	private final StoredFileService storedFileService;

	public UserProfilePhotoService(UserProfilePhotoRepository userProfilePhotoRepository,
			AppUserReferenceRepository appUserReferenceRepository, StoredFileService storedFileService) {
		this.userProfilePhotoRepository = userProfilePhotoRepository;
		this.appUserReferenceRepository = appUserReferenceRepository;
		this.storedFileService = storedFileService;
	}

	@Transactional
	public UserProfilePhotoDto uploadOwnPhoto(String keycloakSubject, MultipartFile file) {
		AppUserReference user = findActiveUserBySubject(keycloakSubject);
		return uploadForUser(user.id(), user.id(), file);
	}

	@Transactional
	public UserProfilePhotoDto uploadForUser(UUID userId, UUID uploadedByUserId, MultipartFile file) {
		AppUserReference user = findActiveUserById(userId);
		userProfilePhotoRepository.findByUserId(user.id()).ifPresent(this::deleteExistingPhoto);

		StoredFileEntity storedFile = storedFileService.upload(file, FileCategory.USER_PROFILE_PHOTO, user.id(),
				uploadedByUserId, "User profile photo");
		try {
			UserProfilePhotoEntity entity = new UserProfilePhotoEntity();
			entity.setUserId(user.id());
			entity.setStoredFileId(storedFile.getId());
			return toDto(userProfilePhotoRepository.save(entity), storedFile);
		} catch (RuntimeException ex) {
			storedFileService.hardDelete(storedFile);
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public UserProfilePhotoDto findOwnPhoto(String keycloakSubject) {
		AppUserReference user = findActiveUserBySubject(keycloakSubject);
		return findForUser(user.id());
	}

	@Transactional(readOnly = true)
	public UserProfilePhotoDto findForUser(UUID userId) {
		UserProfilePhotoEntity link = findLinkByUserId(userId);
		StoredFileEntity file = storedFileService.findActiveByIdAndCategory(link.getStoredFileId(),
				FileCategory.USER_PROFILE_PHOTO);
		return toDto(link, file);
	}

	@Transactional(readOnly = true)
	public PresignedUrlDto ownDownloadUrl(String keycloakSubject) {
		AppUserReference user = findActiveUserBySubject(keycloakSubject);
		return downloadUrlForUser(user.id());
	}

	@Transactional(readOnly = true)
	public PresignedUrlDto downloadUrlForUser(UUID userId) {
		UserProfilePhotoEntity link = findLinkByUserId(userId);
		return storedFileService.presignedDownloadUrl(link.getStoredFileId(), FileCategory.USER_PROFILE_PHOTO);
	}

	@Transactional
	public void deleteOwnPhoto(String keycloakSubject) {
		AppUserReference user = findActiveUserBySubject(keycloakSubject);
		deleteForUser(user.id());
	}

	@Transactional
	public void deleteForUser(UUID userId) {
		UserProfilePhotoEntity link = findLinkByUserId(userId);
		deleteExistingPhoto(link);
	}

	private void deleteExistingPhoto(UserProfilePhotoEntity link) {
		StoredFileEntity file = storedFileService.findActiveByIdAndCategory(link.getStoredFileId(),
				FileCategory.USER_PROFILE_PHOTO);
		userProfilePhotoRepository.delete(link);
		storedFileService.hardDelete(file);
	}

	private UserProfilePhotoEntity findLinkByUserId(UUID userId) {
		return userProfilePhotoRepository.findByUserId(userId).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
				"PROFILE_PHOTO_NOT_FOUND", "User profile photo not found"));
	}

	private AppUserReference findActiveUserBySubject(String keycloakSubject) {
		return appUserReferenceRepository.findActiveByKeycloakSubject(keycloakSubject)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
	}

	private AppUserReference findActiveUserById(UUID userId) {
		return appUserReferenceRepository.findActiveById(userId)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
	}

	private UserProfilePhotoDto toDto(UserProfilePhotoEntity link, StoredFileEntity file) {
		return new UserProfilePhotoDto(link.getId(), link.getUserId(), storedFileService.toDto(file),
				link.getCreatedAt());
	}
}

package com.clinica.mariana.restms.storedfile.repository;

import com.clinica.mariana.restms.storedfile.entity.UserProfilePhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfilePhotoRepository extends JpaRepository<UserProfilePhotoEntity, UUID> {

	Optional<UserProfilePhotoEntity> findByUserId(UUID userId);
}

package com.clinica.mariana.restms.users.repository;

import com.clinica.mariana.restms.users.entity.UserProfilePhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfilePhotoRepository extends JpaRepository<UserProfilePhotoEntity, UUID> {

	Optional<UserProfilePhotoEntity> findByUserId(UUID userId);
}

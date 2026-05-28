package com.clinica.mariana.restms.service.repository;

import com.clinica.mariana.restms.service.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {

	List<ServiceEntity> findAllByActiveTrueOrderByNameAsc();

	List<ServiceEntity> findAllByCategoryIdAndActiveTrueOrderByNameAsc(UUID categoryId);

	List<ServiceEntity> findAllByCategoryIdOrderByNameAsc(UUID categoryId);

	@Query("SELECT COUNT(s) > 0 FROM ServiceEntity s WHERE LOWER(s.name) = LOWER(:name) AND s.category.id = :categoryId")
	boolean existsByNameIgnoreCaseAndCategoryId(@Param("name") String name, @Param("categoryId") UUID categoryId);

	@Query("SELECT COUNT(s) > 0 FROM ServiceEntity s WHERE LOWER(s.name) = LOWER(:name) AND s.category.id = :categoryId AND s.id <> :id")
	boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(@Param("name") String name,
			@Param("categoryId") UUID categoryId, @Param("id") UUID id);
}

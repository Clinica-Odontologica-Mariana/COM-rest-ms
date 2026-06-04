package com.clinica.mariana.restms.address.repository;

import com.clinica.mariana.restms.address.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

	Page<AddressEntity> findAllByOrderByCityAscStreetAsc(Pageable pageable);
}

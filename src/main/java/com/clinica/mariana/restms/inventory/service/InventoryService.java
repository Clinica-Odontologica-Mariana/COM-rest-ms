package com.clinica.mariana.restms.inventory.service;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.inventory.dto.InventoryItemCreateDto;
import com.clinica.mariana.restms.inventory.dto.InventoryItemDto;
import com.clinica.mariana.restms.inventory.dto.InventoryItemUpdateDto;
import com.clinica.mariana.restms.inventory.dto.StockMovementCreateDto;
import com.clinica.mariana.restms.inventory.dto.StockMovementDto;
import com.clinica.mariana.restms.inventory.entity.InventoryItemEntity;
import com.clinica.mariana.restms.inventory.entity.StockMovementEntity;
import com.clinica.mariana.restms.inventory.repository.InventoryItemRepository;
import com.clinica.mariana.restms.inventory.repository.StockMovementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

	private static final String ITEM_NOT_FOUND = "Inventory item not found";

	private final InventoryItemRepository itemRepository;
	private final StockMovementRepository movementRepository;
	private final ClinicRepository clinicRepository;

	public InventoryService(InventoryItemRepository itemRepository, StockMovementRepository movementRepository,
			ClinicRepository clinicRepository) {
		this.itemRepository = itemRepository;
		this.movementRepository = movementRepository;
		this.clinicRepository = clinicRepository;
	}

	@Transactional
	public InventoryItemDto createItem(InventoryItemCreateDto request) {
		validateClinic(request.clinicId());
		if (itemRepository.existsByClinicIdAndName(request.clinicId(), request.name())) {
			throw new AppException(HttpStatus.CONFLICT, "ALREADY_EXISTS",
					"Inventory item name already exists for clinic");
		}
		InventoryItemEntity entity = new InventoryItemEntity();
		entity.setClinicId(request.clinicId());
		apply(entity, request.itemType(), request.name(), request.description(), request.sku(), request.unit(),
				request.minimumQuantity());
		entity.setCurrentQuantity(BigDecimal.ZERO);
		entity.setActive(true);
		return toDto(itemRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<InventoryItemDto> findItemsByClinic(UUID clinicId) {
		validateClinic(clinicId);
		return itemRepository.findAllByClinicIdAndActiveTrueOrderByNameAsc(clinicId).stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public InventoryItemDto findItemById(UUID id) {
		return toDto(findItem(id));
	}

	@Transactional
	public InventoryItemDto updateItem(UUID id, InventoryItemUpdateDto request) {
		InventoryItemEntity entity = findItem(id);
		if (itemRepository.existsByClinicIdAndNameAndIdNot(entity.getClinicId(), request.name(), id)) {
			throw new AppException(HttpStatus.CONFLICT, "ALREADY_EXISTS",
					"Inventory item name already exists for clinic");
		}
		apply(entity, request.itemType(), request.name(), request.description(), request.sku(), request.unit(),
				request.minimumQuantity());
		return toDto(itemRepository.save(entity));
	}

	@Transactional
	public void deleteItem(UUID id) {
		InventoryItemEntity entity = findItem(id);
		if (!entity.isActive()) {
			return;
		}
		entity.setActive(false);
		itemRepository.save(entity);
	}

	@Transactional
	public StockMovementDto createMovement(StockMovementCreateDto request) {
		InventoryItemEntity item = findItem(request.inventoryItemId());
		if (!item.isActive()) {
			throw new AppException(HttpStatus.CONFLICT, "INTERNAL_ERROR", "Inventory item is inactive");
		}

		BigDecimal newQuantity = switch (request.movementType()) {
			case "IN" -> item.getCurrentQuantity().add(request.quantity());
			case "OUT" -> item.getCurrentQuantity().subtract(request.quantity());
			case "ADJUSTMENT" -> request.quantity();
			default -> throw new AppException(HttpStatus.BAD_REQUEST, "INTERNAL_ERROR", "Invalid movement type");
		};
		if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
			throw new AppException(HttpStatus.CONFLICT, "INTERNAL_ERROR",
					"Stock movement would make inventory negative");
		}

		item.setCurrentQuantity(newQuantity);
		itemRepository.save(item);

		StockMovementEntity movement = new StockMovementEntity();
		movement.setInventoryItemId(request.inventoryItemId());
		movement.setMovementType(request.movementType());
		movement.setQuantity(request.quantity());
		movement.setReason(request.reason());
		return toDto(movementRepository.save(movement));
	}

	@Transactional(readOnly = true)
	public List<StockMovementDto> findMovementsByItem(UUID itemId) {
		findItem(itemId);
		return movementRepository.findAllByInventoryItemIdOrderByCreatedAtDesc(itemId).stream().map(this::toDto)
				.toList();
	}

	private InventoryItemEntity findItem(UUID id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", ITEM_NOT_FOUND));
	}

	private void validateClinic(UUID clinicId) {
		if (!clinicRepository.existsById(clinicId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", "Clinic not found");
		}
	}

	private void apply(InventoryItemEntity entity, String itemType, String name, String description, String sku,
			String unit, BigDecimal minimumQuantity) {
		entity.setItemType(itemType);
		entity.setName(name);
		entity.setDescription(description);
		entity.setSku(sku);
		entity.setUnit(unit);
		entity.setMinimumQuantity(minimumQuantity);
	}

	private InventoryItemDto toDto(InventoryItemEntity entity) {
		return new InventoryItemDto(entity.getId(), entity.getClinicId(), entity.getItemType(), entity.getName(),
				entity.getDescription(), entity.getSku(), entity.getUnit(), entity.getCurrentQuantity(),
				entity.getMinimumQuantity(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private StockMovementDto toDto(StockMovementEntity entity) {
		return new StockMovementDto(entity.getId(), entity.getInventoryItemId(), entity.getMovementType(),
				entity.getQuantity(), entity.getReason(), entity.getCreatedByUserId(), entity.getCreatedAt());
	}
}

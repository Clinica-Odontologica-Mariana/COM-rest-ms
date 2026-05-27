package com.clinica.mariana.restms.inventory.unit;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.inventory.dto.StockMovementCreateDto;
import com.clinica.mariana.restms.inventory.entity.InventoryItemEntity;
import com.clinica.mariana.restms.inventory.repository.InventoryItemRepository;
import com.clinica.mariana.restms.inventory.repository.StockMovementRepository;
import com.clinica.mariana.restms.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

	@Mock
	private InventoryItemRepository itemRepository;

	@Mock
	private StockMovementRepository movementRepository;

	@Mock
	private ClinicRepository clinicRepository;

	@InjectMocks
	private InventoryService service;

	@Test
	void shouldRejectOutboundMovementThatMakesStockNegative() {
		UUID itemId = UUID.randomUUID();
		InventoryItemEntity item = new InventoryItemEntity();
		item.setCurrentQuantity(BigDecimal.ONE);
		item.setActive(true);

		when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

		StockMovementCreateDto request = new StockMovementCreateDto(itemId, "OUT", BigDecimal.TEN, "usage", null);

		assertThatThrownBy(() -> service.createMovement(request)).isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("inventory negative");
	}
}

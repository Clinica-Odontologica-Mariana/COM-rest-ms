package com.clinica.mariana.restms.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateDto(
		@NotBlank(message = "street is required") @Size(max = 150, message = "street must have at most 150 characters") String street,

		@Size(max = 20, message = "number must have at most 20 characters") String number,

		@Size(max = 100, message = "complement must have at most 100 characters") String complement,

		@Size(max = 100, message = "neighborhood must have at most 100 characters") String neighborhood,

		@NotBlank(message = "city is required") @Size(max = 100, message = "city must have at most 100 characters") String city,

		@NotBlank(message = "state is required") @Pattern(regexp = "^[A-Z]{2}$", message = "state must contain exactly 2 uppercase letters") String state,

		@NotBlank(message = "zipCode is required") @Pattern(regexp = "^[0-9]{8}$", message = "zipCode must contain exactly 8 digits") String zipCode) {
}

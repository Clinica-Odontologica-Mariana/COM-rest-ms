package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record SocialLinkUpdateDto(

        @NotNull(message = "platformId is required")
        UUID platformId,

        @NotBlank(message = "url is required")
        @Pattern(regexp = "(?i)^https?://.*", message = "url must start with http:// or https://")
        String url
){
}
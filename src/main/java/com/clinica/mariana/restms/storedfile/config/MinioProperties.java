package com.clinica.mariana.restms.storedfile.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(@NotBlank(message = "MINIO_ENDPOINT is required") String endpoint,
		@NotBlank(message = "MINIO_ACCESS_KEY is required") String accessKey,
		@NotBlank(message = "MINIO_SECRET_KEY is required") String secretKey,
		@NotBlank(message = "MINIO_BUCKET is required") String bucket, String publicEndpoint, String region,
		@Min(1) int presignedUrlExpirationSeconds, boolean createBucketIfMissing) {
}

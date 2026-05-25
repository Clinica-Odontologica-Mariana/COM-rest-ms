package com.clinica.mariana.restms.storedfile.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties({MinioProperties.class, FileValidationProperties.class})
public class MinioConfig {

	@Bean
	MinioClient minioClient(MinioProperties properties) {
		MinioClient.Builder builder = MinioClient.builder().endpoint(properties.endpoint())
				.credentials(properties.accessKey(), properties.secretKey());
		if (StringUtils.hasText(properties.region())) {
			builder.region(properties.region());
		}
		return builder.build();
	}
}

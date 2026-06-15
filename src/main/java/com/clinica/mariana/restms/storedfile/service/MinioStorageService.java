package com.clinica.mariana.restms.storedfile.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

	private final MinioClient minioClient;
	private final MinioClient presignClient;
	private final MinioProperties properties;

	public MinioStorageService(MinioClient minioClient, MinioProperties properties) {
		this.minioClient = minioClient;
		this.presignClient = buildPresignClient(properties);
		this.properties = properties;
	}

	public UploadResult upload(byte[] content, String objectKey, String mimeType) {
		ensureBucket();
		try {
			minioClient.putObject(PutObjectArgs.builder().bucket(properties.bucket()).object(objectKey)
					.stream(new ByteArrayInputStream(content), content.length, -1).contentType(mimeType).build());
			return new UploadResult(properties.bucket(), objectKey, null);
		} catch (Exception ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "MINIO_UPLOAD_FAILED", "Failed to upload file to storage",
					List.of(rootCause(ex)));
		}
	}

	public PresignedObjectUrl presignedDownloadUrl(String objectKey) {
		try {
			String url = presignClient.getPresignedObjectUrl(
					GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(properties.bucket()).object(objectKey)
							.expiry(properties.presignedUrlExpirationSeconds(), TimeUnit.SECONDS).build());
			return new PresignedObjectUrl(url,
					OffsetDateTime.now().plusSeconds(properties.presignedUrlExpirationSeconds()));
		} catch (Exception ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "MINIO_PRESIGNED_URL_FAILED",
					"Failed to generate download URL", List.of(rootCause(ex)));
		}
	}

	public void remove(String objectKey) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.bucket()).object(objectKey).build());
		} catch (Exception ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "MINIO_REMOVE_FAILED", "Failed to remove file from storage",
					List.of(rootCause(ex)));
		}
	}

	private void ensureBucket() {
		if (!properties.createBucketIfMissing()) {
			return;
		}
		try {
			boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket()).build());
			if (!exists) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
			}
		} catch (Exception ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "MINIO_BUCKET_CHECK_FAILED",
					"Failed to prepare storage bucket", List.of(rootCause(ex)));
		}
	}

	private MinioClient buildPresignClient(MinioProperties properties) {
		MinioClient.Builder builder = MinioClient.builder().endpoint(
				StringUtils.hasText(properties.publicEndpoint()) ? properties.publicEndpoint() : properties.endpoint())
				.credentials(properties.accessKey(), properties.secretKey());
		if (StringUtils.hasText(properties.region())) {
			builder.region(properties.region());
		}
		return builder.build();
	}

	private String rootCause(Exception ex) {
		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return cause.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
	}

	public record UploadResult(String bucketName, String objectKey, String etag) {
	}

	public record PresignedObjectUrl(String url, OffsetDateTime expiresAt) {
	}
}

package com.clinica.mariana.restms.storedfile.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.config.FileValidationProperties;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;

@Service
public class FileValidationService {

	private static final int SIGNATURE_BYTES = 12;

	private final FileValidationProperties properties;

	public FileValidationService(FileValidationProperties properties) {
		this.properties = properties;
	}

	public void validate(FileCategory category, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new AppException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File must not be empty");
		}

		FileValidationProperties.FilePolicy policy = policyFor(category);
		if (file.getSize() > policy.maxSizeBytes()) {
			throw new AppException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "File exceeds configured limit");
		}

		String mimeType = file.getContentType();
		Set<String> allowedMimeTypes = policy.allowedMimeTypeSet();
		if (mimeType == null || !allowedMimeTypes.contains(mimeType)) {
			throw new AppException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE",
					"File MIME type is not allowed");
		}

		validateSignature(mimeType, file);
	}

	public long maxSizeBytes(FileCategory category) {
		return policyFor(category).maxSizeBytes();
	}

	public Set<String> allowedMimeTypes(FileCategory category) {
		return policyFor(category).allowedMimeTypeSet();
	}

	private FileValidationProperties.FilePolicy policyFor(FileCategory category) {
		if (category == null) {
			throw new AppException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_CATEGORY", "File category is required");
		}
		return switch (category) {
			case USER_PROFILE_PHOTO -> properties.profilePhoto();
			case ODONTOGRAM -> properties.odontogram();
			case MEDICAL_RECORD_ATTACHMENT -> throw new AppException(HttpStatus.BAD_REQUEST,
					"UNSUPPORTED_FILE_CATEGORY", "Medical record attachments cannot be uploaded through this endpoint");
			case CERTIFICATE -> throw new AppException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_CATEGORY",
					"Certificates cannot be uploaded through this endpoint");
			case LEGACY -> throw new AppException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_CATEGORY",
					"Legacy files cannot be uploaded through this endpoint");
		};
	}

	private void validateSignature(String mimeType, MultipartFile file) {
		try {
			byte[] signature = file.getBytes();
			if (signature.length == 0) {
				throw new AppException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "File must not be empty");
			}
			int length = Math.min(signature.length, SIGNATURE_BYTES);
			byte[] header = new byte[length];
			System.arraycopy(signature, 0, header, 0, length);
			if (!matchesSignature(mimeType, header)) {
				throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_FILE_SIGNATURE",
						"File content does not match MIME type");
			}
		} catch (IOException ex) {
			throw new AppException(HttpStatus.BAD_REQUEST, "FILE_READ_ERROR", "Failed to read uploaded file");
		}
	}

	private boolean matchesSignature(String mimeType, byte[] header) {
		String hex = HexFormat.of().formatHex(header).toLowerCase();
		return switch (mimeType) {
			case "image/jpeg" -> hex.startsWith("ffd8ff");
			case "image/png" -> hex.startsWith("89504e470d0a1a0a");
			case "image/webp" ->
				header.length >= 12 && new String(header, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
						&& new String(header, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
			case "application/pdf" -> hex.startsWith("25504446");
			default -> false;
		};
	}
}

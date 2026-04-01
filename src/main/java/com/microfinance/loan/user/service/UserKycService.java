package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserKycService {

	private final UserRepository userRepository;
	private final KycDocumentRepository kycDocumentRepository;
	private final FileStorageService fileStorageService;

	public UserKycService(UserRepository userRepository,
						  KycDocumentRepository kycDocumentRepository,
						  FileStorageService fileStorageService) {
		this.userRepository = userRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.fileStorageService = fileStorageService;
	}

	public KycStatusResponse.KycDocumentItem uploadKycDocument(Long userId, KycUploadRequest request, MultipartFile file) throws IOException {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		String fileUrl = fileStorageService.storeFile(file, "kyc/" + userId);

		KycDocument document = KycDocument.builder()
				.user(user)
				.fileType(request.getDocumentType())
				.documentNumber(request.getDocumentNumber())
				.fileUrl(fileUrl)
				.fileName(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-file")))
				.mimeType(file.getContentType())
				.fileSize(String.valueOf(file.getSize()))
				.build();

		KycDocument saved = kycDocumentRepository.save(document);
		return mapToItem(saved);
	}

	public KycStatusResponse getKycStatus(Long userId) {
		List<KycStatusResponse.KycDocumentItem> documents = kycDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::mapToItem)
				.collect(Collectors.toList());

		return KycStatusResponse.builder()
				.overallKycStatus(null)
				.documents(documents)
				.build();
	}

	private KycStatusResponse.KycDocumentItem mapToItem(KycDocument document) {
		return KycStatusResponse.KycDocumentItem.builder()
				.documentId(document.getId())
				.documentType(document.getFileType())
				.documentNumber(document.getDocumentNumber())
				.fileUrl(document.getFileUrl())
				.fileName(document.getFileName())
				.mimeType(document.getMimeType())
				.fileSize(document.getFileSize())
				.verificationStatus(document.getVerificationStatus())
				.rejectionReason(document.getRejectedReason())
				.officerRemarks(document.getOfficerRemarks())
				.version(document.getVersion())
				.isActive(document.getIsActive())
				.uploadedAt(document.getCreatedAt())
				.reviewedAt(document.getReviewedAt())
				.build();
	}
}

package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
	private final UserProfileRepository userProfileRepository;
	private final FileStorageService fileStorageService;

	public UserKycService(UserRepository userRepository,
						  KycDocumentRepository kycDocumentRepository,
						  UserProfileRepository userProfileRepository,
						  FileStorageService fileStorageService) {
		this.userRepository = userRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.userProfileRepository = userProfileRepository;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public KycStatusResponse.KycDocumentItem uploadKycDocument(Long userId, KycUploadRequest request, MultipartFile file) throws IOException {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		KycDocument activeDocument = kycDocumentRepository
				.findTopByUserIdAndDocumentTypeAndIsActiveTrueOrderByVersionDesc(userId, request.getDocumentType())
				.orElse(null);

		int nextVersion = 1;
		if (activeDocument != null) {
			activeDocument.setIsActive(false);
			kycDocumentRepository.save(activeDocument);
			nextVersion = activeDocument.getVersion() + 1;
		}

		String fileUrl = fileStorageService.storeFile(file, "kyc/" + userId + "/" + request.getDocumentType().name().toLowerCase());

		KycDocument document = KycDocument.builder()
				.user(user)
				.documentType(request.getDocumentType())
				.documentNumber(request.getDocumentNumber())
				.fileUrl(fileUrl)
				.fileName(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-file")))
				.mimeType(file.getContentType())
				.fileSize(String.valueOf(file.getSize()))
				.version(nextVersion)
				.isActive(true)
				.verificationStatus(KycStatus.PENDING)
				.build();

		KycDocument saved = kycDocumentRepository.save(document);

		// Any re-upload requires re-validation by officer.
		profile.setKycStatus(KycStatus.PENDING);
		userProfileRepository.save(profile);

		return mapToItem(saved);
	}

	@Transactional
	public KycStatusResponse submitKyc(Long userId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		boolean hasAadhaar = kycDocumentRepository.existsByUserIdAndDocumentTypeAndIsActiveTrue(userId, KycDocumentType.AADHAAR);
		boolean hasPan = kycDocumentRepository.existsByUserIdAndDocumentTypeAndIsActiveTrue(userId, KycDocumentType.PAN);

		if (!hasAadhaar || !hasPan) {
			throw new IllegalArgumentException("Please upload both Aadhaar and PAN documents before submitting KYC.");
		}

		profile.setKycStatus(KycStatus.IN_REVIEW);
		userProfileRepository.save(profile);

		return getKycStatus(userId);
	}

	public KycStatusResponse getKycStatus(Long userId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		List<KycStatusResponse.KycDocumentItem> documents = kycDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::mapToItem)
				.collect(Collectors.toList());

		return KycStatusResponse.builder()
				.overallKycStatus(profile.getKycStatus())
				.documents(documents)
				.build();
	}

	private KycStatusResponse.KycDocumentItem mapToItem(KycDocument document) {
		return KycStatusResponse.KycDocumentItem.builder()
				.documentId(document.getId())
				.documentType(document.getDocumentType())
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

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

		String  normalizedDocNumber = normalizeDocumentNumber(request.getDocumentType(), request.getDocumentNumber());

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
				.documentNumber(normalizedDocNumber)
				.build();

		KycDocument saved = kycDocumentRepository.save(document);

		syncProfileDocNumber(profile, request.getDocumentType(), normalizedDocNumber);
		profile.setKycStatus(calculateOverallKycStatus(userId));
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

		profile.setKycStatus(calculateOverallKycStatus(userId));
		userProfileRepository.save(profile);

		return getKycStatus(userId);
	}

	@Transactional
	public KycStatusResponse deletePendingKycDocument(Long userId, Long documentId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		KycDocument document = kycDocumentRepository.findByIdAndUserId(documentId, userId)
				.orElseThrow(() -> new IllegalArgumentException("KYC document not found for user: " + documentId));

		if (document.getVerificationStatus() == KycStatus.VERIFIED) {
			throw new IllegalArgumentException("Verified KYC document cannot be deleted");
		}

		if (document.getVerificationStatus() != KycStatus.PENDING) {
			throw new IllegalArgumentException("Only pending KYC document can be deleted");
		}

		document.setIsActive(false);
		kycDocumentRepository.save(document);

		if (!kycDocumentRepository.existsByUserIdAndDocumentTypeAndIsActiveTrue(userId, document.getDocumentType())) {
			if (document.getDocumentType() == KycDocumentType.AADHAAR) {
				profile.setAadhaarNumber(null);
			} else if (document.getDocumentType() == KycDocumentType.PAN) {
				profile.setPanNumber(null);
			}
		}

		profile.setKycStatus(calculateOverallKycStatus(userId));
		userProfileRepository.save(profile);

		return getKycStatus(userId);
	}

	@Transactional
	public KycStatusResponse.KycDocumentItem editPendingKycDocument(Long userId,
														 Long documentId,
														 String documentNumber,
														 MultipartFile file) throws IOException {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		KycDocument document = kycDocumentRepository.findByIdAndUserId(documentId, userId)
				.orElseThrow(() -> new IllegalArgumentException("KYC document not found for user: " + documentId));

		if (document.getVerificationStatus() == KycStatus.VERIFIED) {
			throw new IllegalArgumentException("Verified KYC document cannot be edited");
		}

		if (document.getVerificationStatus() != KycStatus.PENDING) {
			throw new IllegalArgumentException("Only pending KYC document can be edited");
		}

		boolean hasDocumentNumber = StringUtils.hasText(documentNumber);
		boolean hasFile = file != null && !file.isEmpty();
		if (!hasDocumentNumber && !hasFile) {
			throw new IllegalArgumentException("Provide document number or file to edit KYC document");
		}

		if (hasDocumentNumber) {
			String normalizedDocNumber = normalizeDocumentNumber(document.getDocumentType(), documentNumber);
			document.setDocumentNumber(normalizedDocNumber);
			syncProfileDocNumber(profile, document.getDocumentType(), normalizedDocNumber);
		}

		if (hasFile) {
			String fileUrl = fileStorageService.storeFile(file,
					"kyc/" + userId + "/" + document.getDocumentType().name().toLowerCase());
			document.setFileUrl(fileUrl);
			document.setFileName(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-file")));
			document.setMimeType(file.getContentType());
			document.setFileSize(String.valueOf(file.getSize()));
		}

		document.setOfficerRemarks(null);
		document.setRejectedReason(null);
		document.setReviewedAt(null);

		KycDocument saved = kycDocumentRepository.save(document);

		profile.setKycStatus(calculateOverallKycStatus(userId));
		userProfileRepository.save(profile);

		return mapToItem(saved);
	}

	public KycStatusResponse getKycStatus(Long userId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		KycStatus computedStatus = calculateOverallKycStatus(userId);
		if (profile.getKycStatus() != computedStatus) {
			profile.setKycStatus(computedStatus);
			userProfileRepository.save(profile);
		}

		List<KycStatusResponse.KycDocumentItem> documents = kycDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::mapToItem)
				.collect(Collectors.toList());

		return KycStatusResponse.builder()
				.overallKycStatus(computedStatus)
				.documents(documents)
				.build();
	}

	private KycStatus calculateOverallKycStatus(Long userId) {
		List<KycDocument> activeDocuments = kycDocumentRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
		if (activeDocuments.isEmpty()) {
			return KycStatus.PENDING;
		}

		boolean aadhaarVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.AADHAAR, KycStatus.VERIFIED
		);
		boolean panVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.PAN, KycStatus.VERIFIED
		);
		if (aadhaarVerified && panVerified) {
			return KycStatus.VERIFIED;
		}

		boolean anyRejectedOrResubmit = activeDocuments.stream().anyMatch(d ->
				d.getVerificationStatus() == KycStatus.REJECTED || d.getVerificationStatus() == KycStatus.RESUBMIT_REQUIRED);
		if (anyRejectedOrResubmit) {
			return KycStatus.RESUBMIT_REQUIRED;
		}

		return KycStatus.IN_REVIEW;
	}

	private void syncProfileDocNumber(UserProfile profile, KycDocumentType documentType, String documentNumber) {
		if(documentNumber == null) return;
		String normalized = normalizeDocumentNumber(documentType, documentNumber);

		if(documentType == KycDocumentType.AADHAAR){
			profile.setAadhaarNumber(normalized);
		}else if(documentType == KycDocumentType.PAN){
			profile.setPanNumber(normalized);
		}
	}

	private String normalizeDocumentNumber(KycDocumentType documentType, String value) {
        String cleaned = value == null ? null : value.trim();
        if(cleaned == null) return null;

        if(documentType == KycDocumentType.AADHAAR){
            return cleaned.replaceAll("\\s+", "");
        }
        if(documentType == KycDocumentType.PAN){
            return cleaned.replaceAll("\\s+", "").toUpperCase();
        }
        return cleaned;
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

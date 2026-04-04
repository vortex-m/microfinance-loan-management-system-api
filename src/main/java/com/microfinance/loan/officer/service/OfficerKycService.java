package com.microfinance.loan.officer.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import com.microfinance.loan.officer.entity.KycReview;
import com.microfinance.loan.officer.dto.request.KycReviewDecisionRequest;
import com.microfinance.loan.officer.dto.response.KycReviewResponse;
import com.microfinance.loan.officer.repository.KycReviewRepository;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OfficerKycService {

	private final UserProfileRepository userProfileRepository;
	private final KycDocumentRepository kycDocumentRepository;
	private final KycReviewRepository kycReviewRepository;
	private final UserDetailsServiceImpl userDetailsService;

	public OfficerKycService(UserProfileRepository userProfileRepository,
							 KycDocumentRepository kycDocumentRepository,
							 KycReviewRepository kycReviewRepository,
							 UserDetailsServiceImpl userDetailsService) {
		this.userProfileRepository = userProfileRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.kycReviewRepository = kycReviewRepository;
		this.userDetailsService = userDetailsService;
	}

	@Transactional
	public KycReviewResponse reviewUserKyc(Long userId, Long documentId, KycReviewDecisionRequest request) {
		KycDocument document = kycDocumentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("KYC document not found: " + documentId));

		if (!document.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("KYC document does not belong to user: " + userId);
		}

		if (request.getReviewStatus() == KycStatus.PENDING || request.getReviewStatus() == KycStatus.IN_REVIEW) {
			throw new IllegalArgumentException("Review status must be VERIFIED, REJECTED or RESUBMIT_REQUIRED.");
		}

		Users reviewer = getAuthenticatedOfficer();

		document.setVerificationStatus(request.getReviewStatus());
		document.setOfficerRemarks(request.getReviewRemarks());
		document.setReviewedAt(LocalDateTime.now());
		document.setReviewedBy(reviewer);

		if (request.getReviewStatus() == KycStatus.VERIFIED) {
			document.setRejectedReason(null);
		} else {
			document.setRejectedReason(request.getRejectionReason());
		}

		KycDocument savedDoc = kycDocumentRepository.save(document);

		KycReview review = KycReview.builder()
				.kycReviewCode(generateReviewCode())
				.kycDocument(savedDoc)
				.officer(reviewer)
				.reviewStatus(request.getReviewStatus())
				.resubmissionRequested(Boolean.TRUE.equals(request.getResubmissionRequested()))
				.resubmissionInstructions(request.getResubmissionInstructions())
				.rejectionReason(request.getRejectionReason())
				.officerRemarks(request.getReviewRemarks())
				.reviewedAt(LocalDateTime.now())
				.build();
		kycReviewRepository.save(review);

		UserProfile profile = syncUserKycProfile(userId);

		return KycReviewResponse.builder()
				.documentId(savedDoc.getId())
				.userId(userId)
				.documentNumber(savedDoc.getDocumentNumber())
				.documentStatus(savedDoc.getVerificationStatus())
				.kycStatus(profile.getKycStatus())
				.reviewRemarks(request.getReviewRemarks())
				.rejectionReason(savedDoc.getRejectedReason())
				.reviewedAt(savedDoc.getReviewedAt())
				.build();
	}

	public List<KycReviewResponse> getPendingKycDocuments() {
		return kycDocumentRepository.findByVerificationStatusInOrderByCreatedAtAsc(List.of(KycStatus.PENDING, KycStatus.IN_REVIEW))
				.stream()
				.map(doc -> KycReviewResponse.builder()
						.documentId(doc.getId())
						.userId(doc.getUser().getId())
						.documentNumber(doc.getDocumentNumber())
						.documentStatus(doc.getVerificationStatus())
						.reviewRemarks(doc.getOfficerRemarks())
						.reviewedAt(doc.getReviewedAt())
						.build())
				.toList();
	}

	private UserProfile syncUserKycProfile(Long userId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		boolean aadhaarVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.AADHAAR, KycStatus.VERIFIED
		);
		boolean panVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.PAN, KycStatus.VERIFIED
		);

		boolean hasAadhaar = kycDocumentRepository.existsByUserIdAndDocumentTypeAndIsActiveTrue(userId, KycDocumentType.AADHAAR);
		boolean hasPan = kycDocumentRepository.existsByUserIdAndDocumentTypeAndIsActiveTrue(userId, KycDocumentType.PAN);

		List<KycDocument> activeDocuments = kycDocumentRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
		boolean anyRejectedOrResubmit = activeDocuments.stream().anyMatch(d -> d.getVerificationStatus() == KycStatus.REJECTED || d.getVerificationStatus() == KycStatus.RESUBMIT_REQUIRED);
		boolean anyPendingOrReview = activeDocuments.stream().anyMatch(d -> d.getVerificationStatus() == KycStatus.PENDING || d.getVerificationStatus() == KycStatus.IN_REVIEW);

		if (aadhaarVerified && panVerified) {
			profile.setKycStatus(KycStatus.VERIFIED);
		} else if (anyRejectedOrResubmit) {
			profile.setKycStatus(KycStatus.RESUBMIT_REQUIRED);
		} else if (hasAadhaar && hasPan && anyPendingOrReview) {
			profile.setKycStatus(KycStatus.IN_REVIEW);
		} else {
			profile.setKycStatus(KycStatus.PENDING);
		}

		return userProfileRepository.save(profile);
	}

	private Users getAuthenticatedOfficer() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}

		return userDetailsService.resolveUser(authentication.getName()).orElse(null);
	}

	private String generateReviewCode() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "KYCR-" + ts + "-" + suffix;
	}
}


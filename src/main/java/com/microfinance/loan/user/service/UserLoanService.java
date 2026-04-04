package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.dto.request.LoanApplyRequest;
import com.microfinance.loan.user.dto.response.BankProofUploadResponse;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserLoanService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final LoanApplicationRepository loanApplicationRepository;
	private final KycDocumentRepository kycDocumentRepository;
	private final FileStorageService fileStorageService;

	public UserLoanService(UserRepository userRepository,
						   UserProfileRepository userProfileRepository,
						   LoanApplicationRepository loanApplicationRepository,
						   KycDocumentRepository kycDocumentRepository,
						   FileStorageService fileStorageService) {
		this.userRepository = userRepository;
		this.userProfileRepository = userProfileRepository;
		this.loanApplicationRepository = loanApplicationRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public LoanApplyResponse applyForLoan(Long userId, LoanApplyRequest request) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Complete profile and KYC first."));

		if (profile.getKycStatus() != KycStatus.VERIFIED) {
			throw new IllegalArgumentException("KYC is not approved. Please verify KYC first.");
		}

		boolean aadhaarVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.AADHAAR, KycStatus.VERIFIED
		);
		boolean panVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				userId, KycDocumentType.PAN, KycStatus.VERIFIED
		);
		if (!aadhaarVerified || !panVerified) {
			throw new IllegalArgumentException("Verified Aadhaar and PAN are required before loan application.");
		}

		validateDisbursalFields(request);

		LoanApplication application = LoanApplication.builder()
				.applicationNumber(generateApplicationNumber())
				.user(user)
				.requestedAmount(request.getRequestedAmount())
				.tenureMonths(request.getTenureMonths())
				.loanPurpose(request.getLoanPurpose())
				.loanPurposeDescription(request.getLoanPurposeDescription())
				.userRemarks(request.getUserRemarks())
				.disbursalMode(request.getDisbursalMode())
				.disbursalBankName(safeTrim(request.getDisbursalBankName()))
				.disbursalBankAccount(safeTrim(request.getDisbursalBankAccount()))
				.disbursalIfscCode(normalizeIfsc(request.getDisbursalIfscCode()))
				.disbursalBankProofUrl(safeTrim(request.getDisbursalBankProofUrl()))
				.disbursalBankProofFileName(safeTrim(request.getDisbursalBankProofFileName()))
				.build();

		LoanApplication saved = loanApplicationRepository.save(application);
		return LoanApplyResponse.builder()
				.loanApplicationId(saved.getId())
				.applicationNumber(saved.getApplicationNumber())
				.status(saved.getStatus())
				.requestedAmount(saved.getRequestedAmount())
				.tenureMonths(saved.getTenureMonths())
				.loanPurpose(saved.getLoanPurpose())
				.disbursalMode(saved.getDisbursalMode())
				.disbursalBankName(saved.getDisbursalBankName())
				.disbursalBankAccount(saved.getDisbursalBankAccount())
				.disbursalIfscCode(saved.getDisbursalIfscCode())
				.disbursalBankProofUrl(saved.getDisbursalBankProofUrl())
				.disbursalBankProofFileName(saved.getDisbursalBankProofFileName())
				.appliedAt(saved.getCreatedAt())
				.build();
	}

	public BankProofUploadResponse uploadBankProof(Long userId, MultipartFile file) throws IOException {
		userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		String fileUrl = fileStorageService.storeFile(file, "loan-bank-proof/" + userId);
		return BankProofUploadResponse.builder()
				.fileUrl(fileUrl)
				.fileName(Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-bank-proof"))
				.mimeType(file.getContentType())
				.fileSize(file.getSize())
				.uploadedAt(LocalDateTime.now())
				.build();
	}

	private void validateDisbursalFields(LoanApplyRequest request) {
		if (request.getDisbursalMode() == DisbursalMode.BANK_TRANSFER
				&& (!StringUtils.hasText(request.getDisbursalBankName())
					|| !StringUtils.hasText(request.getDisbursalBankAccount())
					|| !StringUtils.hasText(request.getDisbursalIfscCode())
					|| !StringUtils.hasText(request.getDisbursalBankProofUrl()))) {
			throw new IllegalArgumentException("Bank details and bank proof are required for BANK_TRANSFER disbursal mode.");
		}

		if (request.getDisbursalMode() == DisbursalMode.CASH) {
			request.setDisbursalBankName(null);
			request.setDisbursalBankAccount(null);
			request.setDisbursalIfscCode(null);
			request.setDisbursalBankProofUrl(null);
			request.setDisbursalBankProofFileName(null);
		}
	}

	private String generateApplicationNumber() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "APP-" + ts + "-" + suffix;
	}

	private String safeTrim(String value) {
		return value == null ? null : value.trim();
	}

	private String normalizeIfsc(String ifsc) {
		return ifsc == null ? null : ifsc.trim().toUpperCase();
	}
}

package com.microfinance.loan.user.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.OriginChannel;
import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.agent.dto.request.AgentLoanApplyForUserRequest;
import com.microfinance.loan.user.dto.request.LoanApplyRequest;
import com.microfinance.loan.user.dto.response.BankProofUploadResponse;
import com.microfinance.loan.user.dto.response.LoanApplyResponse;
import com.microfinance.loan.user.dto.response.LoanDetailResponse;
import com.microfinance.loan.user.dto.response.LoanStatusResponse;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserLoanService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final LoanApplicationRepository loanApplicationRepository;
	private final KycDocumentRepository kycDocumentRepository;
	private final FileStorageService fileStorageService;
	private final LoanRepository loanRepository;
	private final AgentProfileRepository agentProfileRepository;

	public UserLoanService(UserRepository userRepository,
						   UserProfileRepository userProfileRepository,
						   LoanApplicationRepository loanApplicationRepository,
						   KycDocumentRepository kycDocumentRepository,
						   FileStorageService fileStorageService,
						   LoanRepository loanRepository,
						   AgentProfileRepository agentProfileRepository) {
		this.userRepository = userRepository;
		this.userProfileRepository = userProfileRepository;
		this.loanApplicationRepository = loanApplicationRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.fileStorageService = fileStorageService;
		this.loanRepository = loanRepository;
		this.agentProfileRepository = agentProfileRepository;
	}

	@Transactional
	public LoanApplyResponse applyForLoan(Long userId, LoanApplyRequest request) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Complete profile and KYC first."));

		validateLoanEligibility(user, profile);
		validateTenure(request.getTenureMonths());

		validateDisbursalFields(request);

		LoanApplication application = LoanApplication.builder()
				.applicationNumber(generateApplicationNumber())
				.user(user)
				.originChannel(OriginChannel.SELF_SERVICE)
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
		return toApplyResponse(saved);
	}

	@Transactional
	public LoanApplyResponse applyForLoanByAgent(Long agentUserId, AgentLoanApplyForUserRequest request) {
		Users user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new IllegalArgumentException("Only ACTIVE users can apply for loan");
		}

		AgentProfile agentProfile = agentProfileRepository.findByUsersIdWithBranch(agentUserId)
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user: " + agentUserId));

		if (agentProfile.getBranchProfile() == null) {
			throw new IllegalArgumentException("Agent is not mapped to a branch");
		}

		UserProfile profile = userProfileRepository.findByUsersId(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("Complete profile and KYC first."));

		if (profile.getBranchProfile() == null) {
			throw new IllegalArgumentException("User is not mapped to any branch");
		}

		if (!agentProfile.getBranchProfile().getBranchCode().equalsIgnoreCase(profile.getBranchProfile().getBranchCode())) {
			throw new IllegalArgumentException("Agent can apply loan only for users in same branch");
		}

		validateLoanEligibility(user, profile);
		validateTenure(request.getTenureMonths());

		if (profile.getCreditScore() == null || profile.getRiskScore() == null || profile.getScoreUpdatedAt() == null) {
			throw new IllegalArgumentException("User score is not available. Run scoring before loan application.");
		}

		if (profile.getRiskScore() > 0.90d) {
			throw new IllegalArgumentException("User risk score is too high for agent-assisted application. Escalate to manager.");
		}

		LoanApplyRequest normalized = LoanApplyRequest.builder()
				.requestedAmount(request.getRequestedAmount())
				.tenureMonths(request.getTenureMonths())
				.loanPurpose(request.getLoanPurpose())
				.loanPurposeDescription(request.getLoanPurposeDescription())
				.userRemarks(request.getUserRemarks())
				.disbursalMode(request.getDisbursalMode())
				.disbursalBankName(request.getDisbursalBankName())
				.disbursalBankAccount(request.getDisbursalBankAccount())
				.disbursalIfscCode(request.getDisbursalIfscCode())
				.disbursalBankProofUrl(request.getDisbursalBankProofUrl())
				.disbursalBankProofFileName(request.getDisbursalBankProofFileName())
				.build();

		validateDisbursalFields(normalized);

		LoanApplication application = LoanApplication.builder()
				.applicationNumber(generateApplicationNumber())
				.user(user)
				.originChannel(OriginChannel.AGENT_ASSISTED)
				.createdByAgent(agentProfile.getUsers())
				.assignedAgent(agentProfile.getUsers())
				.requestedAmount(normalized.getRequestedAmount())
				.tenureMonths(normalized.getTenureMonths())
				.loanPurpose(normalized.getLoanPurpose())
				.loanPurposeDescription(normalized.getLoanPurposeDescription())
				.userRemarks(normalized.getUserRemarks())
				.disbursalMode(normalized.getDisbursalMode())
				.disbursalBankName(safeTrim(normalized.getDisbursalBankName()))
				.disbursalBankAccount(safeTrim(normalized.getDisbursalBankAccount()))
				.disbursalIfscCode(normalizeIfsc(normalized.getDisbursalIfscCode()))
				.disbursalBankProofUrl(safeTrim(normalized.getDisbursalBankProofUrl()))
				.disbursalBankProofFileName(safeTrim(normalized.getDisbursalBankProofFileName()))
				.build();

		LoanApplication saved = loanApplicationRepository.save(application);
		return toApplyResponse(saved);
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

	@Transactional(readOnly = true)
	public LoanStatusResponse getMyLoanStatuses(Long userId) {
		userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		List<LoanStatusResponse.LoanItem> items = loanApplicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(application -> LoanStatusResponse.LoanItem.builder()
						.loanApplicationId(application.getId())
						.applicationNumber(application.getApplicationNumber())
						.requestedAmount(application.getRequestedAmount())
						.tenureMonths(application.getTenureMonths())
						.loanPurpose(application.getLoanPurpose())
						.disbursalMode(application.getDisbursalMode())
						.status(application.getStatus())
						.rejectionReason(application.getRejectionReason())
						.appliedAt(application.getCreatedAt())
						.updatedAt(application.getUpdatedAt())
						.build())
				.toList();

		return LoanStatusResponse.builder().loans(items).build();
	}

	@Transactional(readOnly = true)
	public LoanDetailResponse getMyLoanDetail(Long userId, Long loanApplicationId) {
		LoanApplication application = loanApplicationRepository.findByIdAndUserId(loanApplicationId, userId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));

		Loan loan = loanRepository.findByLoanApplicationId(application.getId()).orElse(null);

		return LoanDetailResponse.builder()
				.loanApplicationId(application.getId())
				.applicationNumber(application.getApplicationNumber())
				.status(application.getStatus())
				.requestedAmount(application.getRequestedAmount())
				.tenureMonths(application.getTenureMonths())
				.loanPurpose(application.getLoanPurpose())
				.loanPurposeDescription(application.getLoanPurposeDescription())
				.userRemarks(application.getUserRemarks())
				.disbursalMode(application.getDisbursalMode())
				.disbursalBankName(application.getDisbursalBankName())
				.disbursalBankAccount(application.getDisbursalBankAccount())
				.disbursalIfscCode(application.getDisbursalIfscCode())
				.loanNumber(loan != null ? loan.getLoanNumber() : null)
				.approvedAmount(application.getApprovedAmount())
				.interestRate(loan != null ? loan.getInterestRate() : null)
				.interestType(loan != null ? loan.getInterestType() : null)
				.emiAmount(loan != null ? loan.getEmiAmount() : null)
				.processingFee(loan != null ? loan.getProcessingFee() : null)
				.totalInterestPayable(loan != null ? loan.getTotalInterestPayable() : null)
				.totalAmountPayable(loan != null ? loan.getTotalAmountPayable() : null)
				.totalEmis(loan != null ? loan.getTotalEmis() : null)
				.emisPaid(loan != null ? loan.getEmisPaid() : null)
				.emisPending(loan != null ? loan.getEmisPending() : null)
				.emisOverdue(loan != null ? loan.getEmisOverdue() : null)
				.outstandingPrincipal(loan != null ? loan.getOutstandingPrincipal() : null)
				.totalPaidAmount(loan != null ? loan.getTotalPaidAmount() : null)
				.disbursementDate(loan != null ? loan.getDisbursementDate() : null)
				.firstEmiDate(loan != null ? loan.getFirstEmiDate() : null)
				.lastEmiDate(loan != null ? loan.getLastEmiDate() : null)
				.officerRemarks(application.getOfficerRemarks())
				.rejectionReason(application.getRejectionReason())
				.appliedAt(application.getCreatedAt())
				.disbursedAt(application.getDisbursedAt())
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

	private void validateLoanEligibility(Users user, UserProfile profile) {
		if (!Boolean.TRUE.equals(user.getIsHome())) {
			throw new IllegalArgumentException("Please complete onboarding before applying for loan.");
		}

		if (profile.getKycStatus() != KycStatus.VERIFIED) {
			throw new IllegalArgumentException("KYC is not approved. Please verify KYC first.");
		}

		boolean aadhaarVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				user.getId(), KycDocumentType.AADHAAR, KycStatus.VERIFIED
		);
		boolean panVerified = kycDocumentRepository.existsByUserIdAndDocumentTypeAndVerificationStatusAndIsActiveTrue(
				user.getId(), KycDocumentType.PAN, KycStatus.VERIFIED
		);
		if (!aadhaarVerified || !panVerified) {
			throw new IllegalArgumentException("Verified Aadhaar and PAN are required before loan application.");
		}
	}

	private void validateTenure(Integer tenureMonths) {
		if (tenureMonths == null || tenureMonths < 1 || tenureMonths > 36) {
			throw new IllegalArgumentException("Loan tenure must be between 1 and 36 months.");
		}
	}

	private LoanApplyResponse toApplyResponse(LoanApplication saved) {
		return LoanApplyResponse.builder()
				.loanApplicationId(saved.getId())
				.applicationNumber(saved.getApplicationNumber())
				.userId(saved.getUser() != null ? saved.getUser().getId() : null)
				.createdByAgentId(saved.getCreatedByAgent() != null ? saved.getCreatedByAgent().getId() : null)
				.assignedAgentId(saved.getAssignedAgent() != null ? saved.getAssignedAgent().getId() : null)
				.assignedOfficerId(saved.getAssignedOfficer() != null ? saved.getAssignedOfficer().getId() : null)
				.status(saved.getStatus())
				.originChannel(saved.getOriginChannel())
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

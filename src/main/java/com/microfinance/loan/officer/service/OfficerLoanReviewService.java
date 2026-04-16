package com.microfinance.loan.officer.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.officer.dto.request.LoanDecisionRequest;
import com.microfinance.loan.officer.dto.response.LoanReviewResponse;
import com.microfinance.loan.officer.entity.LoanReview;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.LoanReviewRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OfficerLoanReviewService {

	private final LoanApplicationRepository loanApplicationRepository;
	private final OfficerProfileRepository officerProfileRepository;
	private final LoanReviewRepository loanReviewRepository;
	private final CurrentUserService currentUserService;
	private final UserProfileRepository userProfileRepository;
	private final AgentProfileRepository agentProfileRepository;

	public OfficerLoanReviewService(LoanApplicationRepository loanApplicationRepository,
									OfficerProfileRepository officerProfileRepository,
									LoanReviewRepository loanReviewRepository,
									CurrentUserService currentUserService,
									UserProfileRepository userProfileRepository,
									AgentProfileRepository agentProfileRepository) {
		this.loanApplicationRepository = loanApplicationRepository;
		this.officerProfileRepository = officerProfileRepository;
		this.loanReviewRepository = loanReviewRepository;
		this.currentUserService = currentUserService;
		this.userProfileRepository = userProfileRepository;
		this.agentProfileRepository = agentProfileRepository;
	}

	@Transactional
	public LoanReviewResponse assignAgent(org.springframework.security.core.Authentication authentication,
										 Long loanApplicationId,
										 LoanAgentAssignmentRequest request) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(officerProfile, application);

		if (application.getStatus() == LoanStatus.REJECTED
				|| application.getStatus() == LoanStatus.APPROVED
				|| application.getStatus() == LoanStatus.DISBURSED
				|| application.getStatus() == LoanStatus.CLOSED) {
			throw new IllegalArgumentException("Agent assignment is not allowed in final state: " + application.getStatus());
		}

		AgentProfile agentProfile = agentProfileRepository.findByUsersIdWithBranch(request.getAgentUserId())
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found: " + request.getAgentUserId()));

		if (agentProfile.getBranchProfile() == null
				|| !officerProfile.getBranchProfile().getBranchCode().equalsIgnoreCase(agentProfile.getBranchProfile().getBranchCode())) {
			throw new IllegalArgumentException("Officer can assign only same-branch agents");
		}

		if (agentProfile.getAgentStatus() != AgentStatus.ACTIVE) {
			throw new IllegalArgumentException("Only ACTIVE agents can be assigned");
		}

		application.setAssignedAgent(agentProfile.getUsers());
		loanApplicationRepository.save(application);
		return toResponse(application);
	}

	@Transactional(readOnly = true)
	public List<LoanReviewResponse> getPendingLoanQueue(org.springframework.security.core.Authentication authentication) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		String branchCode = officerProfile.getBranchProfile().getBranchCode();

		List<LoanApplication> applications = loanApplicationRepository.findByBranchCodeAndStatuses(
				branchCode,
				List.of(LoanStatus.PENDING, LoanStatus.UNDER_REVIEW)
		);

		return applications.stream().map(this::toResponse).toList();
	}

	@Transactional
	public LoanReviewResponse submitDecision(org.springframework.security.core.Authentication authentication,
											 Long loanApplicationId,
											 LoanDecisionRequest request) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		Users officerUser = officerProfile.getUsers();

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));

		validateBranchScope(officerProfile, application);

		if (application.getStatus() == LoanStatus.REJECTED
				|| application.getStatus() == LoanStatus.APPROVED
				|| application.getStatus() == LoanStatus.DISBURSED
				|| application.getStatus() == LoanStatus.CLOSED) {
			throw new IllegalArgumentException("Loan application is already in final state: " + application.getStatus());
		}

		LoanReview review = loanReviewRepository.findByLoanApplicationId(application.getId())
				.orElseGet(() -> LoanReview.builder()
						.reviewCode(generateReviewCode())
						.loanApplication(application)
						.officer(officerUser)
						.reviewStartedAt(LocalDateTime.now())
						.build());

		review.setOfficer(officerUser);
		review.setDecision(request.getDecision());
		review.setOfficerRemarks(request.getOfficerRemarks());
		review.setDecisionTakenAt(LocalDateTime.now());
		review.setLoanStatusAtReview(application.getStatus());

		switch (request.getDecision()) {
			case APPROVED -> {
				if (request.getApprovedAmount() == null || request.getApprovedAmount() <= 0) {
					throw new IllegalArgumentException("Approved amount is required when decision is APPROVED");
				}
				if (request.getApprovedTenureMonths() == null
						|| request.getApprovedTenureMonths() < 1
						|| request.getApprovedTenureMonths() > 36) {
					throw new IllegalArgumentException("Approved tenure must be between 1 and 36 months");
				}

				application.setApprovedAmount(request.getApprovedAmount());
				application.setTenureMonths(request.getApprovedTenureMonths());
				application.setOfficerRemarks(request.getOfficerRemarks());
				application.setAssignedOfficer(officerUser);
				application.setStatus(LoanStatus.PENDING_MANAGER_APPROVAL);

				review.setApprovedAmount(request.getApprovedAmount());
				review.setApprovedTenureMonths(request.getApprovedTenureMonths());
				review.setRejectionReason(null);
				review.setReVerifyReason(null);
			}
			case REJECTED -> {
				if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
					throw new IllegalArgumentException("Rejection reason is required when decision is REJECTED");
				}
				application.setRejectionReason(request.getRejectionReason().trim());
				application.setOfficerRemarks(request.getOfficerRemarks());
				application.setAssignedOfficer(officerUser);
				application.setStatus(LoanStatus.REJECTED);

				review.setRejectionReason(request.getRejectionReason().trim());
				review.setApprovedAmount(null);
				review.setApprovedTenureMonths(null);
				review.setReVerifyReason(null);
			}
			case RE_VERIFY -> {
				if (request.getReVerifyReason() == null || request.getReVerifyReason().trim().isEmpty()) {
					throw new IllegalArgumentException("Re-verify reason is required when decision is RE_VERIFY");
				}
				application.setOfficerRemarks(request.getOfficerRemarks());
				application.setAssignedOfficer(officerUser);
				application.setStatus(LoanStatus.UNDER_REVIEW);

				review.setReVerifyReason(request.getReVerifyReason().trim());
				review.setApprovedAmount(null);
				review.setApprovedTenureMonths(null);
				review.setRejectionReason(null);
			}
			case PENDING -> throw new IllegalArgumentException("Officer decision cannot be PENDING");
		}

		loanApplicationRepository.save(application);
		loanReviewRepository.save(review);

		return toResponse(application);
	}

	private OfficerProfile getActiveOfficerProfile(org.springframework.security.core.Authentication authentication) {
		Long officerUserId = currentUserService.getCurrentUserId(authentication);
		OfficerProfile profile = officerProfileRepository.findByUsersIdWithBranch(officerUserId)
				.orElseThrow(() -> new IllegalArgumentException("Officer profile not found for user: " + officerUserId));

		if (profile.getBranchProfile() == null) {
			throw new IllegalArgumentException("Officer is not mapped to any branch");
		}
		if (profile.getOfficerStatus() != OfficerStatus.ACTIVE) {
			throw new IllegalArgumentException("Only ACTIVE officers can review loan applications");
		}
		return profile;
	}

	private void validateBranchScope(OfficerProfile officerProfile, LoanApplication application) {
		if (application.getUser() == null) {
			throw new IllegalArgumentException("Loan application user mapping is missing");
		}

		String branchCode = officerProfile.getBranchProfile().getBranchCode();
		boolean sameBranch = userProfileRepository.findByUsersId(application.getUser().getId())
				.map(up -> up.getBranchProfile() != null
						&& branchCode.equalsIgnoreCase(up.getBranchProfile().getBranchCode()))
				.orElse(false);

		if (!sameBranch) {
			throw new IllegalArgumentException("Officer can review only own branch applications");
		}
	}

	private LoanReviewResponse toResponse(LoanApplication application) {
		return LoanReviewResponse.builder()
				.loanApplicationId(application.getId())
				.applicationNumber(application.getApplicationNumber())
				.userId(application.getUser().getId())
				.userName(application.getUser().getName())
				.requestedAmount(application.getRequestedAmount())
				.tenureMonths(application.getTenureMonths())
				.loanPurpose(application.getLoanPurpose())
				.disbursalMode(application.getDisbursalMode())
				.disbursalBankName(application.getDisbursalBankName())
				.disbursalBankAccount(application.getDisbursalBankAccount())
				.disbursalIfscCode(application.getDisbursalIfscCode())
				.status(application.getStatus())
				.appliedAt(application.getCreatedAt())
				.build();
	}

	private String generateReviewCode() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "REV-" + ts + "-" + suffix;
	}
}


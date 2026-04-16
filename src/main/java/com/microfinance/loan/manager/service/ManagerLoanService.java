package com.microfinance.loan.manager.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.ManagerDecision;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.loan.service.LoanService;
import com.microfinance.loan.manager.dto.request.BulkAgentReassignRequest;
import com.microfinance.loan.manager.dto.request.ManagerLoanDecisionRequest;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.manager.dto.response.BulkAgentReassignResponse;
import com.microfinance.loan.manager.dto.response.ManagerLoanResponse;
import com.microfinance.loan.manager.entity.AuditLog;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.AuditLogRepository;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.officer.entity.LoanReview;
import com.microfinance.loan.officer.repository.LoanReviewRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerLoanService {

	private final LoanApplicationRepository loanApplicationRepository;
	private final ManagerProfileRepository managerProfileRepository;
	private final CurrentUserService currentUserService;
	private final LoanReviewRepository loanReviewRepository;
	private final AgentProfileRepository agentProfileRepository;
	private final LoanService loanService;
	private final LoanRepository loanRepository;
	private final UserProfileRepository userProfileRepository;
	private final AuditLogRepository auditLogRepository;

	public ManagerLoanService(LoanApplicationRepository loanApplicationRepository,
							  ManagerProfileRepository managerProfileRepository,
							  CurrentUserService currentUserService,
							  LoanReviewRepository loanReviewRepository,
							  AgentProfileRepository agentProfileRepository,
							  LoanService loanService,
							  LoanRepository loanRepository,
							  UserProfileRepository userProfileRepository,
							  AuditLogRepository auditLogRepository) {
		this.loanApplicationRepository = loanApplicationRepository;
		this.managerProfileRepository = managerProfileRepository;
		this.currentUserService = currentUserService;
		this.loanReviewRepository = loanReviewRepository;
		this.agentProfileRepository = agentProfileRepository;
		this.loanService = loanService;
		this.loanRepository = loanRepository;
		this.userProfileRepository = userProfileRepository;
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional(readOnly = true)
	public List<ManagerLoanResponse> getManagerQueue(Authentication authentication) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		String branchCode = managerProfile.getBranchProfile().getBranchCode();

		return loanApplicationRepository.findByBranchCodeAndStatus(branchCode, LoanStatus.PENDING_MANAGER_APPROVAL)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public ManagerLoanResponse decideLoan(Authentication authentication,
										  Long loanApplicationId,
										  ManagerLoanDecisionRequest request) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		Users managerUser = managerProfile.getUsers();

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(managerProfile, application);

		if (application.getStatus() != LoanStatus.PENDING_MANAGER_APPROVAL) {
			throw new IllegalArgumentException("Loan must be in PENDING_MANAGER_APPROVAL status for manager decision");
		}

		LoanReview review = loanReviewRepository.findByLoanApplicationId(application.getId())
				.orElseThrow(() -> new IllegalArgumentException("Officer review record not found for application: " + application.getId()));
		review.setManager(managerUser);
		review.setManagerDecision(request.getDecision());
		review.setManagerDecisionTakenAt(java.time.LocalDateTime.now());
		review.setManagerRemarks(request.getManagerRemarks());

		if (request.getDecision() == ManagerDecision.REJECTED) {
			if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
				throw new IllegalArgumentException("Rejection reason is required when manager rejects a loan");
			}
			application.setStatus(LoanStatus.REJECTED);
			application.setRejectionReason(request.getRejectionReason().trim());
			review.setManagerFlagReason(request.getRejectionReason().trim());
			loanReviewRepository.save(review);
			loanApplicationRepository.save(application);
			return toResponse(application);
		}

		if (request.getDecision() == ManagerDecision.RETURN_TO_OFFICER) {
			application.setStatus(LoanStatus.UNDER_REVIEW);
			review.setReVerifyInstructions(request.getManagerRemarks());
			loanReviewRepository.save(review);
			loanApplicationRepository.save(application);
			return toResponse(application);
		}

		if (request.getDecision() != ManagerDecision.APPROVED) {
			throw new IllegalArgumentException("Manager decision cannot be PENDING");
		}

		Users assignedAgent = resolveAgentForApproval(managerProfile, application, request.getAgentUserId());
		application.setAssignedAgent(assignedAgent);
		application.setStatus(LoanStatus.APPROVED);
		loanApplicationRepository.save(application);
		loanReviewRepository.save(review);
		recordAssignmentAudit(application, managerUser, "LOAN_AGENT_ASSIGNED",
				"Loan approved with assigned agent", "APPROVED");

		Users officer = application.getAssignedOfficer();
		Loan bookedLoan = loanService.createLoanFromApplication(application, officer, managerUser, assignedAgent);
		return toResponse(application, bookedLoan);
	}

	@Transactional
	public ManagerLoanResponse confirmBankDisbursal(Authentication authentication,
													Long loanApplicationId,
													String transactionReference) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		Users managerUser = managerProfile.getUsers();

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(managerProfile, application);

		if (application.getStatus() != LoanStatus.APPROVED) {
			throw new IllegalArgumentException("Only APPROVED applications can be marked as disbursed");
		}

		if (application.getDisbursalMode() != DisbursalMode.BANK_TRANSFER) {
			throw new IllegalArgumentException("Bank disbursal is allowed only for BANK_TRANSFER mode loans");
		}

		if (!StringUtils.hasText(transactionReference)) {
			throw new IllegalArgumentException("Transaction reference is required for bank disbursal");
		}

		Loan loan = loanRepository.findByLoanApplicationId(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Booked loan not found for application: " + loanApplicationId));
		loanService.markBankDisbursed(loan, transactionReference.trim(), managerUser);

		application.setDisbursalReference(transactionReference.trim());
		application.setDisbursedAt(java.time.LocalDateTime.now());
		application.setStatus(LoanStatus.DISBURSED);
		loanApplicationRepository.save(application);

		return toResponse(application, loan);
	}

	@Transactional
	public ManagerLoanResponse assignAgent(Authentication authentication,
									   Long loanApplicationId,
									   LoanAgentAssignmentRequest request) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		Users managerUser = managerProfile.getUsers();

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(managerProfile, application);

		Users assignedAgent = validateAndResolveRequestedAgent(managerProfile, request.getAgentUserId());
		application.setAssignedAgent(assignedAgent);
		loanApplicationRepository.save(application);

		loanRepository.findByLoanApplicationId(application.getId()).ifPresent(loan -> {
			loan.setVerifiedByAgent(assignedAgent);
			if (loan.getDisbursalMode() == DisbursalMode.CASH) {
				loan.setCashHandoverAgent(assignedAgent);
			}
			loanRepository.save(loan);
		});

		recordAssignmentAudit(application, managerUser, "LOAN_AGENT_ASSIGNED", request.getReason(), "ASSIGNED");
		return toResponse(application);
	}

	@Transactional
	public ManagerLoanResponse reassignAgent(Authentication authentication,
										 Long loanApplicationId,
										 LoanAgentAssignmentRequest request) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		Users managerUser = managerProfile.getUsers();

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(managerProfile, application);

		if (application.getAssignedAgent() == null) {
			throw new IllegalArgumentException("Loan has no assigned agent. Use assign-agent endpoint.");
		}

		Users newAgent = validateAndResolveRequestedAgent(managerProfile, request.getAgentUserId());
		Long oldAgentId = application.getAssignedAgent().getId();
		if (oldAgentId.equals(newAgent.getId())) {
			throw new IllegalArgumentException("New agent must be different from currently assigned agent");
		}

		application.setAssignedAgent(newAgent);
		loanApplicationRepository.save(application);

		loanRepository.findByLoanApplicationId(application.getId()).ifPresent(loan -> {
			loan.setVerifiedByAgent(newAgent);
			if (loan.getDisbursalMode() == DisbursalMode.CASH) {
				loan.setCashHandoverAgent(newAgent);
			}
			loanRepository.save(loan);
		});

		recordAssignmentAudit(application, managerUser, "LOAN_AGENT_REASSIGNED",
				request.getReason() + " (oldAgentId=" + oldAgentId + ", newAgentId=" + newAgent.getId() + ")",
				"REASSIGNED");
		return toResponse(application);
	}

	@Transactional
	public BulkAgentReassignResponse reassignBulk(Authentication authentication,
											BulkAgentReassignRequest request) {
		ManagerProfile managerProfile = getEligibleManager(authentication);
		Users managerUser = managerProfile.getUsers();

		if (request.getOldAgentUserId().equals(request.getNewAgentUserId())) {
			throw new IllegalArgumentException("Old agent and new agent cannot be same");
		}

		String branchCode = managerProfile.getBranchProfile().getBranchCode();
		Users newAgent = validateAndResolveRequestedAgent(managerProfile, request.getNewAgentUserId());

		List<LoanStatus> activeApplicationStatuses = Arrays.asList(
				LoanStatus.PENDING,
				LoanStatus.UNDER_REVIEW,
				LoanStatus.PENDING_MANAGER_APPROVAL,
				LoanStatus.APPROVED,
				LoanStatus.DISBURSED
		);

		List<LoanApplication> applications = loanApplicationRepository.findByBranchCodeAndAssignedAgentIdAndStatuses(
				branchCode,
				request.getOldAgentUserId(),
				activeApplicationStatuses
		);

		for (LoanApplication application : applications) {
			Long oldAgentId = application.getAssignedAgent() != null ? application.getAssignedAgent().getId() : null;
			application.setAssignedAgent(newAgent);
			recordAssignmentAudit(application, managerUser, "LOAN_AGENT_REASSIGNED_BULK",
					request.getReason() + " (oldAgentId=" + oldAgentId + ", newAgentId=" + newAgent.getId() + ")",
					"BULK_REASSIGNED");
		}
		loanApplicationRepository.saveAll(applications);

		List<Loan> bookedLoans = loanRepository.findByBranchCodeAndVerifiedByAgentIdAndLoanStatusIn(
				branchCode,
				request.getOldAgentUserId(),
				Arrays.asList(LoanStatus.APPROVED, LoanStatus.DISBURSED)
		);

		for (Loan loan : bookedLoans) {
			loan.setVerifiedByAgent(newAgent);
			if (loan.getDisbursalMode() == DisbursalMode.CASH) {
				loan.setCashHandoverAgent(newAgent);
			}
		}
		loanRepository.saveAll(bookedLoans);

		return BulkAgentReassignResponse.builder()
				.oldAgentUserId(request.getOldAgentUserId())
				.newAgentUserId(request.getNewAgentUserId())
				.loanApplicationsUpdated(applications.size())
				.bookedLoansUpdated(bookedLoans.size())
				.reason(request.getReason())
				.build();
	}

	private Users resolveAgentForApproval(ManagerProfile managerProfile,
									  LoanApplication application,
									  Long requestedAgentUserId) {
		String branchCode = managerProfile.getBranchProfile().getBranchCode();

		if (requestedAgentUserId != null) {
			return validateAndResolveRequestedAgent(managerProfile, requestedAgentUserId);
		}

		if (application.getCreatedByAgent() != null) {
			Long creatorAgentId = application.getCreatedByAgent().getId();
			AgentProfile creatorProfile = agentProfileRepository.findByUsersIdWithBranch(creatorAgentId).orElse(null);
			if (creatorProfile != null
					&& creatorProfile.getBranchProfile() != null
					&& branchCode.equalsIgnoreCase(creatorProfile.getBranchProfile().getBranchCode())
					&& creatorProfile.getAgentStatus() == AgentStatus.ACTIVE) {
				return creatorProfile.getUsers();
			}
		}

		return agentProfileRepository.findActiveAvailableByBranchCode(branchCode, AgentStatus.ACTIVE, AgentAvailability.AVAILABLE)
				.stream()
				.min(Comparator.comparing(ap -> ap.getTotalVerifications() == null ? 0 : ap.getTotalVerifications()))
				.map(AgentProfile::getUsers)
				.orElseThrow(() -> new IllegalArgumentException("No ACTIVE and AVAILABLE agent found for branch " + branchCode));
	}

	private Users validateAndResolveRequestedAgent(ManagerProfile managerProfile, Long agentUserId) {
		AgentProfile requested = agentProfileRepository.findByUsersIdWithBranch(agentUserId)
				.orElseThrow(() -> new IllegalArgumentException("Requested agent profile not found: " + agentUserId));

		if (requested.getBranchProfile() == null
				|| !managerProfile.getBranchProfile().getBranchCode().equalsIgnoreCase(requested.getBranchProfile().getBranchCode())) {
			throw new IllegalArgumentException("Requested agent is not mapped to manager branch");
		}
		if (requested.getAgentStatus() != AgentStatus.ACTIVE) {
			throw new IllegalArgumentException("Requested agent is not ACTIVE");
		}
		return requested.getUsers();
	}

	private void recordAssignmentAudit(LoanApplication application,
									Users performedBy,
									String actionType,
									String remarks,
									String newState) {
		AuditLog auditLog = AuditLog.builder()
				.auditCode(generateAuditCode())
				.performedBy(performedBy)
				.performedByRole(performedBy.getRole() != null ? performedBy.getRole().name() : "MANAGER")
				.loanApplication(application)
				.actionType(actionType)
				.actionDescription("Agent assignment change on loan application")
				.previousState(application.getStatus() != null ? application.getStatus().name() : null)
				.newState(newState)
				.remarks(remarks)
				.build();
		auditLogRepository.save(auditLog);
	}

	private String generateAuditCode() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "AUD-" + ts + "-" + suffix;
	}

	private ManagerProfile getEligibleManager(Authentication authentication) {
		Long managerUserId = currentUserService.getCurrentUserId(authentication);
		ManagerProfile managerProfile = managerProfileRepository.findByUsersIdWithBranch(managerUserId)
				.orElseThrow(() -> new IllegalArgumentException("Manager profile not found for user: " + managerUserId));

		if (managerProfile.getBranchProfile() == null) {
			throw new IllegalArgumentException("Manager is not mapped to branch");
		}

		String dept = managerProfile.getDepartment();
		boolean allowed = dept != null && (dept.equalsIgnoreCase(ManagerDepartment.BRANCH_OPERATIONS.name())
				|| dept.equalsIgnoreCase(ManagerDepartment.LOAN_OPERATIONS.name()));
		if (!allowed) {
			throw new IllegalArgumentException("Manager department is not allowed for loan decision operations");
		}
		return managerProfile;
	}

	private void validateBranchScope(ManagerProfile managerProfile, LoanApplication application) {
		String managerBranchCode = managerProfile.getBranchProfile().getBranchCode();
		boolean sameBranch = userProfileRepository.findByUsersId(application.getUser().getId())
				.map(up -> up.getBranchProfile() != null
						&& managerBranchCode.equalsIgnoreCase(up.getBranchProfile().getBranchCode()))
				.orElse(false);
		if (!sameBranch) {
			throw new IllegalArgumentException("Manager can access only own branch applications");
		}
	}

	private ManagerLoanResponse toResponse(LoanApplication application) {
		return toResponse(application, loanRepository.findByLoanApplicationId(application.getId()).orElse(null));
	}

	private ManagerLoanResponse toResponse(LoanApplication application, Loan loan) {
		return ManagerLoanResponse.builder()
				.loanId(loan != null ? loan.getId() : null)
				.loanApplicationId(application.getId())
				.userId(application.getUser() != null ? application.getUser().getId() : null)
				.createdByAgentId(application.getCreatedByAgent() != null ? application.getCreatedByAgent().getId() : null)
				.assignedAgentId(application.getAssignedAgent() != null ? application.getAssignedAgent().getId() : null)
				.assignedOfficerId(application.getAssignedOfficer() != null ? application.getAssignedOfficer().getId() : null)
				.applicationNumber(application.getApplicationNumber())
				.status(application.getStatus())
				.originChannel(application.getOriginChannel())
				.requestedAmount(application.getRequestedAmount())
				.disbursalMode(application.getDisbursalMode())
				.disbursalBankName(application.getDisbursalBankName())
				.disbursalBankAccount(application.getDisbursalBankAccount())
				.disbursalIfscCode(application.getDisbursalIfscCode())
				.build();
	}
}

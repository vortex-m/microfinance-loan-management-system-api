package com.microfinance.loan.officer.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.entity.VerificationImage;
import com.microfinance.loan.agent.entity.VerificationReport;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.VerificationImageRepository;
import com.microfinance.loan.agent.repository.VerificationReportRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.officer.dto.request.LoanDecisionRequest;
import com.microfinance.loan.officer.dto.response.LoanReviewResponse;
import com.microfinance.loan.officer.dto.response.VerificationEvidenceResponse;
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
	private final AgentTaskRepository agentTaskRepository;
	private final VerificationReportRepository verificationReportRepository;
	private final VerificationImageRepository verificationImageRepository;

	public OfficerLoanReviewService(LoanApplicationRepository loanApplicationRepository,
									OfficerProfileRepository officerProfileRepository,
									LoanReviewRepository loanReviewRepository,
									CurrentUserService currentUserService,
									UserProfileRepository userProfileRepository,
									AgentProfileRepository agentProfileRepository,
									AgentTaskRepository agentTaskRepository,
									VerificationReportRepository verificationReportRepository,
									VerificationImageRepository verificationImageRepository) {
		this.loanApplicationRepository = loanApplicationRepository;
		this.officerProfileRepository = officerProfileRepository;
		this.loanReviewRepository = loanReviewRepository;
		this.currentUserService = currentUserService;
		this.userProfileRepository = userProfileRepository;
		this.agentProfileRepository = agentProfileRepository;
		this.agentTaskRepository = agentTaskRepository;
		this.verificationReportRepository = verificationReportRepository;
		this.verificationImageRepository = verificationImageRepository;
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
		ensureVerificationTask(application, agentProfile.getUsers(), officerProfile.getUsers());
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

	@Transactional(readOnly = true)
	public VerificationEvidenceResponse getVerificationEvidence(org.springframework.security.core.Authentication authentication,
														 Long loanApplicationId) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(officerProfile, application);

		AgentTask verificationTask = agentTaskRepository
				.findTopByLoanApplicationIdAndTaskTypeOrderByCreatedAtDesc(loanApplicationId, AgentTaskType.VERIFICATION)
				.orElse(null);

		VerificationReport report = verificationReportRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(loanApplicationId)
				.orElse(null);

		List<VerificationImage> images = report == null || report.getId() == null
				? List.of()
				: verificationImageRepository.findByVerificationReportIdOrderByCreatedAtDesc(report.getId());

		AgentTask collectionTask = agentTaskRepository
				.findTopByLoanApplicationIdAndTaskTypeOrderByCreatedAtDesc(loanApplicationId, AgentTaskType.CASH_COLLECTION)
				.orElse(null);

		Users assignedAgent = application.getAssignedAgent();
		AgentProfile assignedAgentProfile = assignedAgent == null ? null
				: agentProfileRepository.findByUsersId(assignedAgent.getId()).orElse(null);

		return VerificationEvidenceResponse.builder()
				.loanApplicationId(application.getId())
				.assignedAgentId(assignedAgent != null ? assignedAgent.getId() : null)
				.assignedAgentName(assignedAgent != null ? assignedAgent.getName() : null)
				.verificationTaskId(verificationTask != null ? verificationTask.getId() : null)
				.verificationTaskStatus(verificationTask != null ? verificationTask.getTaskStatus() : null)
				.verificationTaskStartedAt(verificationTask != null ? verificationTask.getStartedAt() : null)
				.verificationTaskCompletedAt(verificationTask != null ? verificationTask.getCompletedAt() : null)
				.verificationStatus(report != null ? report.getVerificationStatus() : null)
				.visitedAt(report != null ? report.getVisitedAt() : null)
				.submittedAt(report != null ? report.getSubmittedAt() : null)
				.visitLatitude(report != null ? report.getVisitLatitude() : null)
				.visitLongitude(report != null ? report.getVisitLongitude() : null)
				.visitAddress(report != null ? report.getVisitAddress() : null)
				.reportSummary(report != null ? report.getAgentRemarks() : null)
				.riskNotes(report != null ? report.getRiskNotes() : null)
				.imageCount(images.size())
				.images(images.stream().map(img -> VerificationEvidenceResponse.ImageEvidenceItem.builder()
						.imageId(img.getId())
						.fileUrl(img.getFileUrl())
						.imageTag(img.getImageTag())
						.description(img.getDescription())
						.captureLatitude(img.getCaptureLatitude())
						.captureLongitude(img.getCaptureLongitude())
						.capturedAt(img.getCapturedAt())
						.build()).toList())
				.collectionPlannedAt(collectionTask != null ? collectionTask.getCollectionPlannedAt() : null)
				.collectionStartedAt(collectionTask != null ? collectionTask.getCollectionStartedAt() : null)
				.collectionStartedLat(collectionTask != null ? collectionTask.getCollectionStartedLat() : null)
				.collectionStartedLng(collectionTask != null ? collectionTask.getCollectionStartedLng() : null)
				.collectionVerifiedAt(collectionTask != null ? collectionTask.getCollectionVerifiedAt() : null)
				.collectionVerifiedLat(collectionTask != null ? collectionTask.getCollectionVerifiedLat() : null)
				.collectionVerifiedLng(collectionTask != null ? collectionTask.getCollectionVerifiedLng() : null)
				.agentLastLatitude(assignedAgentProfile != null ? assignedAgentProfile.getLastLatitude() : null)
				.agentLastLongitude(assignedAgentProfile != null ? assignedAgentProfile.getLastLongitude() : null)
				.agentLastLocationUpdatedAt(assignedAgentProfile != null ? assignedAgentProfile.getLastLocationUpdateAt() : null)
				.build();
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
					requireCompletedVerification(application);
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
		AgentTask verificationTask = agentTaskRepository
				.findTopByLoanApplicationIdAndTaskTypeOrderByCreatedAtDesc(application.getId(), AgentTaskType.VERIFICATION)
				.orElse(null);
		VerificationReport report = verificationReportRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(application.getId())
				.orElse(null);

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
				.verificationTaskStatus(verificationTask != null ? verificationTask.getTaskStatus() : null)
				.verificationStatus(report != null ? report.getVerificationStatus() : null)
				.verificationEvidenceAvailable(report != null && Boolean.TRUE.equals(report.getIsSubmitted()))
				.build();
	}

	private String generateReviewCode() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "REV-" + ts + "-" + suffix;
	}

	private void requireCompletedVerification(LoanApplication application) {
		if (application.getAssignedAgent() == null) {
			throw new IllegalArgumentException("Assign agent and complete verification before approval");
		}

		AgentTask verificationTask = agentTaskRepository
				.findTopByLoanApplicationIdAndTaskTypeOrderByCreatedAtDesc(application.getId(), AgentTaskType.VERIFICATION)
				.orElseThrow(() -> new IllegalArgumentException("No verification task found for this loan application"));

		if (verificationTask.getTaskStatus() != TaskStatus.COMPLETED) {
			throw new IllegalArgumentException("Verification task is not completed yet");
		}

		VerificationReport report = verificationReportRepository.findByTaskId(verificationTask.getId())
				.orElseThrow(() -> new IllegalArgumentException("Verification report not found for completed verification task"));

		if (!Boolean.TRUE.equals(report.getIsSubmitted())) {
			throw new IllegalArgumentException("Verification report is not submitted yet");
		}

		if (report.getVerificationStatus() != VerificationStatus.VERIFIED) {
			throw new IllegalArgumentException("Loan cannot be approved because verification status is " + report.getVerificationStatus());
		}
	}

	private void ensureVerificationTask(LoanApplication application, Users assignedAgent, Users assignedBy) {
		if (application.getId() == null || assignedAgent == null) {
			return;
		}

		boolean alreadyOpen = agentTaskRepository.existsByLoanApplicationIdAndAgentIdAndTaskTypeAndTaskStatusIn(
				application.getId(),
				assignedAgent.getId(),
				AgentTaskType.VERIFICATION,
				List.of(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS)
		);
		if (alreadyOpen) {
			return;
		}

		AgentTask task = AgentTask.builder()
				.taskCode("TSK-VER-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
						+ "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
				.loanApplication(application)
				.agent(assignedAgent)
				.assignedBy(assignedBy)
				.taskType(AgentTaskType.VERIFICATION)
				.taskStatus(TaskStatus.ASSIGNED)
				.taskDescription("Field verification for applicant residence, documents, and livelihood.")
				.priorityLevel("HIGH")
				.deadline(LocalDateTime.now().plusHours(48))
				.build();
		agentTaskRepository.save(task);
	}
}


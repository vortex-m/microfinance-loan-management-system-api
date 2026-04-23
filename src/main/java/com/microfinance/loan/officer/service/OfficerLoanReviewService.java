package com.microfinance.loan.officer.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.entity.VerificationImage;
import com.microfinance.loan.agent.entity.VerificationReport;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.agent.repository.VerificationImageRepository;
import com.microfinance.loan.agent.repository.VerificationReportRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.CashOtpStatus;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.VerificationStatus;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.manager.dto.request.LoanAgentAssignmentRequest;
import com.microfinance.loan.officer.dto.request.LoanDecisionRequest;
import com.microfinance.loan.officer.dto.request.OfficerCashHandoverOtpGenerateRequest;
import com.microfinance.loan.officer.dto.request.OfficerCashHandoverOtpVerifyRequest;
import com.microfinance.loan.officer.dto.response.LoanReviewResponse;
import com.microfinance.loan.officer.dto.response.OfficerAssignableAgentResponse;
import com.microfinance.loan.officer.dto.response.OfficerCashDisbursalQueueResponse;
import com.microfinance.loan.officer.dto.response.OfficerUserProfileResponse;
import com.microfinance.loan.officer.dto.response.VerificationEvidenceResponse;
import com.microfinance.loan.officer.entity.LoanReview;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.LoanReviewRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OfficerLoanReviewService {

	private static final int OTP_MAX_ATTEMPTS = 3;

	private final LoanApplicationRepository loanApplicationRepository;
	private final OfficerProfileRepository officerProfileRepository;
	private final LoanReviewRepository loanReviewRepository;
	private final CurrentUserService currentUserService;
	private final UserProfileRepository userProfileRepository;
	private final AgentProfileRepository agentProfileRepository;
	private final AgentTaskRepository agentTaskRepository;
	private final VerificationReportRepository verificationReportRepository;
	private final VerificationImageRepository verificationImageRepository;
	private final KycDocumentRepository kycDocumentRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;

	public OfficerLoanReviewService(LoanApplicationRepository loanApplicationRepository,
									OfficerProfileRepository officerProfileRepository,
									LoanReviewRepository loanReviewRepository,
									CurrentUserService currentUserService,
									UserProfileRepository userProfileRepository,
									AgentProfileRepository agentProfileRepository,
									AgentTaskRepository agentTaskRepository,
									VerificationReportRepository verificationReportRepository,
														VerificationImageRepository verificationImageRepository,
														KycDocumentRepository kycDocumentRepository,
														PasswordEncoder passwordEncoder,
														MailService mailService) {
		this.loanApplicationRepository = loanApplicationRepository;
		this.officerProfileRepository = officerProfileRepository;
		this.loanReviewRepository = loanReviewRepository;
		this.currentUserService = currentUserService;
		this.userProfileRepository = userProfileRepository;
		this.agentProfileRepository = agentProfileRepository;
		this.agentTaskRepository = agentTaskRepository;
		this.verificationReportRepository = verificationReportRepository;
		this.verificationImageRepository = verificationImageRepository;
		this.kycDocumentRepository = kycDocumentRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailService = mailService;
	}

	@Transactional
	public CashDisbursalOtpResponse generateCashHandoverOtp(Authentication authentication,
											 OfficerCashHandoverOtpGenerateRequest request) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		AgentTask task = getCashDisbursalTaskForOfficer(officerProfile, request.getTaskId());
		LoanApplication application = task.getLoanApplication();

		if (application.getDisbursalMode() != DisbursalMode.CASH) {
			throw new IllegalArgumentException("Cash handover OTP is allowed only for CASH mode loans");
		}
		if (application.getStatus() != LoanStatus.APPROVED) {
			throw new IllegalArgumentException("Loan must be APPROVED before cash handover");
		}
		if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.DECLINED) {
			throw new IllegalArgumentException("Task is not active for cash handover");
		}
		if (task.getAgent() == null || !StringUtils.hasText(task.getAgent().getEmail())) {
			throw new IllegalArgumentException("Assigned agent email is required for OTP handover");
		}

		String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
		application.setCashDisbursalOtpHash(passwordEncoder.encode(otp));
		application.setCashDisbursalOtpStatus(CashOtpStatus.ACTIVE);
		application.setCashDisbursalOtpAttempts(0);
		application.setCashDisbursalOtpRequestedAt(LocalDateTime.now());
		application.setCashDisbursalOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
		application.setCashDisbursalOtpVerifiedAt(null);
		loanApplicationRepository.save(application);

		task.setOtpRequired(true);
		task.setOtpRequestedAt(LocalDateTime.now());
		task.setOtpVerified(false);
		task.setOtpVerifiedAt(null);
		task.setAssignedBy(officerProfile.getUsers());
		agentTaskRepository.save(task);

		mailService.sendCashDisbursalOtp(
				task.getAgent().getEmail(),
				task.getAgent().getName(),
				otp,
				application.getApplicationNumber()
		);

		return CashDisbursalOtpResponse.builder()
				.loanApplicationId(application.getId())
				.otpStatus(application.getCashDisbursalOtpStatus())
				.attempts(application.getCashDisbursalOtpAttempts())
				.expiresAt(application.getCashDisbursalOtpExpiresAt())
				.message("Cash handover OTP sent to assigned agent email")
				.build();
	}

	@Transactional
	public CashDisbursalOtpResponse verifyCashHandoverOtp(Authentication authentication,
									   OfficerCashHandoverOtpVerifyRequest request) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		AgentTask task = getCashDisbursalTaskForOfficer(officerProfile, request.getTaskId());
		LoanApplication application = task.getLoanApplication();

		if (application.getCashDisbursalOtpStatus() != CashOtpStatus.ACTIVE || application.getCashDisbursalOtpHash() == null) {
			throw new IllegalArgumentException("No active handover OTP found for this task");
		}

		if (application.getCashDisbursalOtpExpiresAt() == null || LocalDateTime.now().isAfter(application.getCashDisbursalOtpExpiresAt())) {
			application.setCashDisbursalOtpStatus(CashOtpStatus.EXPIRED);
			loanApplicationRepository.save(application);
			throw new IllegalArgumentException("OTP expired. Please generate a new OTP");
		}

		if (!passwordEncoder.matches(request.getOtp(), application.getCashDisbursalOtpHash())) {
			int attempts = application.getCashDisbursalOtpAttempts() == null ? 0 : application.getCashDisbursalOtpAttempts();
			attempts++;
			application.setCashDisbursalOtpAttempts(attempts);
			if (attempts >= OTP_MAX_ATTEMPTS) {
				application.setCashDisbursalOtpStatus(CashOtpStatus.BLOCKED);
			}
			loanApplicationRepository.save(application);
			throw new IllegalArgumentException(attempts >= OTP_MAX_ATTEMPTS
					? "OTP blocked due to maximum invalid attempts. Generate a new OTP."
					: "Invalid OTP. Please try again.");
		}

		application.setCashDisbursalOtpStatus(CashOtpStatus.USED);
		application.setCashDisbursalOtpVerifiedAt(LocalDateTime.now());
		application.setAssignedOfficer(officerProfile.getUsers());
		loanApplicationRepository.save(application);

		task.setAssignedBy(officerProfile.getUsers());
		task.setOtpVerified(true);
		task.setOtpVerifiedAt(LocalDateTime.now());
		agentTaskRepository.save(task);

		return CashDisbursalOtpResponse.builder()
				.loanApplicationId(application.getId())
				.otpStatus(application.getCashDisbursalOtpStatus())
				.attempts(application.getCashDisbursalOtpAttempts())
				.expiresAt(application.getCashDisbursalOtpExpiresAt())
				.verifiedAt(application.getCashDisbursalOtpVerifiedAt())
				.message("Cash handover verified. Agent can now deliver cash to user")
				.build();
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
	public List<OfficerAssignableAgentResponse> getAssignableAgents(Authentication authentication) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		String branchCode = officerProfile.getBranchProfile().getBranchCode();

		return agentProfileRepository
				.findActiveAvailableByBranchCode(branchCode, AgentStatus.ACTIVE, AgentAvailability.AVAILABLE)
				.stream()
				.map(agent -> OfficerAssignableAgentResponse.builder()
						.agentUserId(agent.getUsers().getId())
						.agentCode(agent.getAgentCode())
						.name(agent.getUsers().getName())
						.phone(agent.getUsers().getPhone())
						.build())
				.toList();
	}

	@Transactional(readOnly = true)
	public List<OfficerCashDisbursalQueueResponse> getCashDisbursalQueue(Authentication authentication) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);
		String branchCode = officerProfile.getBranchProfile().getBranchCode();

		List<AgentTask> tasks = agentTaskRepository.findByTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
				AgentTaskType.CASH_DISBURSAL,
				List.of(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS)
		);

		return tasks.stream()
				.filter(task -> task.getLoanApplication() != null && task.getLoanApplication().getUser() != null)
				.filter(task -> userProfileRepository.findByUsersId(task.getLoanApplication().getUser().getId())
						.map(userProfile -> userProfile.getBranchProfile() != null
								&& branchCode.equalsIgnoreCase(userProfile.getBranchProfile().getBranchCode()))
						.orElse(false))
				.map(this::toCashDisbursalQueueResponse)
				.toList();
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

	@Transactional(readOnly = true)
	public OfficerUserProfileResponse getUserProfile(Authentication authentication, Long loanApplicationId) {
		OfficerProfile officerProfile = getActiveOfficerProfile(authentication);

		LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
				.orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + loanApplicationId));
		validateBranchScope(officerProfile, application);

		Users user = application.getUser();
		UserProfile profile = userProfileRepository.findByUsersId(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + user.getId()));

		List<KycDocument> documents = kycDocumentRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId());

		return OfficerUserProfileResponse.builder()
				.userId(user.getId())
				.name(user.getName())
				.phone(user.getPhone())
				.email(user.getEmail())
				.status(user.getStatus())
				.isHome(user.getIsHome())
				.fatherName(profile.getFatherName())
				.motherName(profile.getMotherName())
				.wifeName(profile.getWifeName())
				.husbandName(profile.getHusbandName())
				.dateOfBirth(profile.getDateOfBirth())
				.gender(profile.getGender())
				.occupation(profile.getOccupation())
				.maritalStatus(profile.getMaritalStatus())
				.monthlyIncome(profile.getMonthlyIncome())
				.street(profile.getStreet())
				.city(profile.getCity())
				.state(profile.getState())
				.pinCode(profile.getPinCode())
				.branchCode(profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchCode() : null)
				.branchName(profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchName() : null)
				.regionCode(profile.getBranchProfile() != null ? profile.getBranchProfile().getRegionCode() : null)
				.kycStatus(profile.getKycStatus())
				.aadhaarNumber(mask(profile.getAadhaarNumber()))
				.panNumber(mask(profile.getPanNumber()))
				.documents(documents.stream().map(this::toKycItem).toList())
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

	private OfficerCashDisbursalQueueResponse toCashDisbursalQueueResponse(AgentTask task) {
		LoanApplication application = task.getLoanApplication();
		Users assignedAgent = task.getAgent();

		return OfficerCashDisbursalQueueResponse.builder()
				.taskId(task.getId())
				.loanApplicationId(application.getId())
				.applicationNumber(application.getApplicationNumber())
				.applicantName(application.getUser().getName())
				.assignedAgentName(assignedAgent != null ? assignedAgent.getName() : null)
				.assignedAgentEmail(assignedAgent != null ? assignedAgent.getEmail() : null)
				.loanAmount(application.getApprovedAmount() != null ? application.getApprovedAmount() : application.getRequestedAmount())
				.otpStatus(application.getCashDisbursalOtpStatus() != null
						? application.getCashDisbursalOtpStatus().name()
						: "NOT_GENERATED")
				.build();
	}

	private AgentTask getCashDisbursalTaskForOfficer(OfficerProfile officerProfile, Long taskId) {
		AgentTask task = agentTaskRepository.findById(taskId)
				.orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
		if (task.getTaskType() != AgentTaskType.CASH_DISBURSAL) {
			throw new IllegalArgumentException("Task is not CASH_DISBURSAL type");
		}
		if (task.getLoanApplication() == null) {
			throw new IllegalArgumentException("Task loan mapping is missing");
		}
		validateBranchScope(officerProfile, task.getLoanApplication());
		return task;
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

		List<TaskStatus> openStatuses = List.of(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS);
		AgentTask openTask = agentTaskRepository
				.findTopByLoanApplicationIdAndTaskTypeAndTaskStatusInOrderByCreatedAtDesc(
						application.getId(),
						AgentTaskType.VERIFICATION,
						openStatuses
				)
				.orElse(null);

		if (openTask != null) {
			if (openTask.getAgent() != null && assignedAgent.getId().equals(openTask.getAgent().getId())) {
				return;
			}

			openTask.setAgent(assignedAgent);
			openTask.setAssignedBy(assignedBy);
			openTask.setTaskStatus(TaskStatus.ASSIGNED);
			openTask.setAcceptedAt(null);
			openTask.setStartedAt(null);
			openTask.setCompletedAt(null);
			openTask.setDeclineReason(null);
			openTask.setDeadline(LocalDateTime.now().plusHours(48));
			agentTaskRepository.save(openTask);
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

	private String mask(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() <= 4) {
			return "****";
		}
		return "****" + trimmed.substring(trimmed.length() - 4);
	}

	private OfficerUserProfileResponse.KycItem toKycItem(KycDocument document) {
		return OfficerUserProfileResponse.KycItem.builder()
				.documentId(document.getId())
				.documentType(document.getDocumentType())
				.documentNumber(mask(document.getDocumentNumber()))
				.verificationStatus(document.getVerificationStatus())
				.fileUrl(document.getFileUrl())
				.reviewedAt(document.getReviewedAt())
				.officerRemarks(document.getOfficerRemarks())
				.rejectedReason(document.getRejectedReason())
				.build();
	}
}


package com.microfinance.loan.ai.service;

import com.microfinance.loan.agent.entity.AgentTask;
import com.microfinance.loan.agent.repository.AgentTaskRepository;
import com.microfinance.loan.ai.dto.CreditScoreResponse;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentTaskType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.TaskStatus;
import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AiService {

	private static final List<LoanStatus> PENDING_FLOW_STATUSES = Arrays.asList(
			LoanStatus.PENDING,
			LoanStatus.UNDER_REVIEW,
			LoanStatus.PENDING_MANAGER_APPROVAL,
			LoanStatus.APPROVED
	);

	private final UserProfileRepository userProfileRepository;
	private final LoanApplicationRepository loanApplicationRepository;
	private final LoanRepository loanRepository;
	private final AgentTaskRepository agentTaskRepository;

	@Value("${ai.scoring.follow-up-risk-threshold:0.65}")
	private double followUpRiskThreshold;

	@Value("${ai.scoring.follow-up-pending-days-threshold:1}")
	private int followUpPendingDaysThreshold;

	public AiService(UserProfileRepository userProfileRepository,
					 LoanApplicationRepository loanApplicationRepository,
					 LoanRepository loanRepository,
					 AgentTaskRepository agentTaskRepository) {
		this.userProfileRepository = userProfileRepository;
		this.loanApplicationRepository = loanApplicationRepository;
		this.loanRepository = loanRepository;
		this.agentTaskRepository = agentTaskRepository;
	}

	@Transactional
	public CreditScoreResponse refreshUserScore(Long userId, boolean createFollowUpTask) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		Users user = profile.getUsers();
		if (user == null) {
			throw new IllegalArgumentException("Invalid profile mapping for user: " + userId);
		}

		ScoreSnapshot snapshot = computeScore(profile);
		profile.setRiskScore(snapshot.riskScore());
		profile.setCreditScore(snapshot.creditScore());
		profile.setScoreUpdatedAt(LocalDateTime.now());
		userProfileRepository.save(profile);

		boolean followUpTaskCreated = false;
		if (createFollowUpTask) {
			followUpTaskCreated = maybeCreateFollowUpTask(user, snapshot);
		}

		return CreditScoreResponse.builder()
				.userId(user.getId())
				.creditScore(profile.getCreditScore())
				.riskScore(profile.getRiskScore())
				.riskLevel(snapshot.riskLevel())
				.maxPendingDays(snapshot.maxPendingDays())
				.overdueEmiCount(snapshot.overdueEmiCount())
				.scoreUpdatedAt(profile.getScoreUpdatedAt())
				.followUpTaskCreated(followUpTaskCreated)
				.build();
	}

	@Transactional
	public int refreshScoresForAllUsers(boolean createFollowUpTask) {
		List<UserProfile> profiles = userProfileRepository.findAll();
		int refreshed = 0;
		for (UserProfile profile : profiles) {
			if (profile.getUsers() == null) {
				continue;
			}
			if (profile.getUsers().getStatus() != UserStatus.ACTIVE) {
				continue;
			}
			refreshUserScore(profile.getUsers().getId(), createFollowUpTask);
			refreshed++;
		}
		return refreshed;
	}

	private ScoreSnapshot computeScore(UserProfile profile) {
		Users user = profile.getUsers();
		List<LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
		List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

		double risk = 0.25d;
		int maxPendingDays = 0;

		if (!Boolean.TRUE.equals(user.getIsHome())) {
			risk += 0.08d;
		}

		if (profile.getKycStatus() != KycStatus.VERIFIED) {
			risk += 0.12d;
		}

		if (profile.getMonthlyIncome() == null || profile.getMonthlyIncome() <= 0) {
			risk += 0.05d;
		}

		for (LoanApplication application : applications) {
			if (!PENDING_FLOW_STATUSES.contains(application.getStatus())) {
				continue;
			}
			int daysPending = daysBetween(application.getCreatedAt(), LocalDateTime.now());
			maxPendingDays = Math.max(maxPendingDays, daysPending);
			risk += Math.min(daysPending * 0.012d, 0.24d);
		}

		int overdueEmiCount = 0;
		for (Loan loan : loans) {
			int overdue = loan.getEmisOverdue() == null ? 0 : loan.getEmisOverdue();
			overdueEmiCount += overdue;
			risk += Math.min(overdue * 0.03d, 0.30d);

			if (Boolean.TRUE.equals(loan.getIsNpa())) {
				risk += 0.20d;
			}
			if (Boolean.TRUE.equals(loan.getIsFraudFlagged())) {
				risk += 0.25d;
			}

			Integer totalEmis = loan.getTotalEmis();
			Integer emisPaid = loan.getEmisPaid();
			if (totalEmis != null && totalEmis > 0 && emisPaid != null && emisPaid > 0) {
				double repaymentRatio = Math.min(1d, (double) emisPaid / totalEmis);
				risk -= repaymentRatio * 0.12d;
			}
		}

		double boundedRisk = clamp(risk, 0d, 1d);
		double creditScore = round(900d - (boundedRisk * 600d));

		return new ScoreSnapshot(
				round(boundedRisk),
				clamp(creditScore, 300d, 900d),
				resolveRiskLevel(boundedRisk),
				maxPendingDays,
				overdueEmiCount
		);
	}

	private boolean maybeCreateFollowUpTask(Users user, ScoreSnapshot snapshot) {
		if (snapshot.riskScore() < followUpRiskThreshold && snapshot.maxPendingDays() < followUpPendingDaysThreshold) {
			return false;
		}

		List<LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
		boolean createdAnyTask = false;
		for (LoanApplication application : applications) {
			if (!PENDING_FLOW_STATUSES.contains(application.getStatus())) {
				continue;
			}
			if (application.getAssignedAgent() == null) {
				continue;
			}

			boolean alreadyOpen = agentTaskRepository.existsByLoanApplicationIdAndAgentIdAndTaskTypeAndTaskStatusIn(
					application.getId(),
					application.getAssignedAgent().getId(),
					AgentTaskType.FOLLOW_UP,
					Arrays.asList(TaskStatus.ASSIGNED, TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS)
			);
			if (alreadyOpen) {
				continue;
			}

			int pendingDays = daysBetween(application.getCreatedAt(), LocalDateTime.now());
			AgentTask task = AgentTask.builder()
					.taskCode(generateTaskCode())
					.loanApplication(application)
					.agent(application.getAssignedAgent())
					.assignedBy(application.getAssignedOfficer())
					.taskType(AgentTaskType.FOLLOW_UP)
					.taskStatus(TaskStatus.ASSIGNED)
					.taskDescription("AI follow-up: risk " + snapshot.riskScore() + ", pending days " + pendingDays)
					.priorityLevel(snapshot.riskScore() >= 0.75d ? "HIGH" : "MEDIUM")
					.deadline(LocalDateTime.now().plusHours(24))
					.build();
			agentTaskRepository.save(task);
			createdAnyTask = true;
		}
		return createdAnyTask;
	}

	private String generateTaskCode() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "TSK-" + ts + "-" + suffix;
	}

	private int daysBetween(LocalDateTime from, LocalDateTime to) {
		if (from == null || to == null) {
			return 0;
		}
		long days = Duration.between(from, to).toDays();
		return (int) Math.max(0, days);
	}

	private String resolveRiskLevel(double riskScore) {
		if (riskScore >= 0.80d) {
			return "CRITICAL";
		}
		if (riskScore >= 0.60d) {
			return "HIGH";
		}
		if (riskScore >= 0.35d) {
			return "MEDIUM";
		}
		return "LOW";
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private double round(double value) {
		return Math.round(value * 100d) / 100d;
	}

	private record ScoreSnapshot(double riskScore,
								 double creditScore,
								 String riskLevel,
								 int maxPendingDays,
								 int overdueEmiCount) {
	}

}


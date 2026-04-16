package com.microfinance.loan.user.service;

import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.user.dto.response.UserDashboardResponse;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDashboardService {

	private final UserProfileRepository userProfileRepository;
	private final LoanApplicationRepository loanApplicationRepository;
	private final LoanRepository loanRepository;
	private final LoanEmiScheduleRepository loanEmiScheduleRepository;

	public UserDashboardService(UserProfileRepository userProfileRepository,
								LoanApplicationRepository loanApplicationRepository,
								LoanRepository loanRepository,
								LoanEmiScheduleRepository loanEmiScheduleRepository) {
		this.userProfileRepository = userProfileRepository;
		this.loanApplicationRepository = loanApplicationRepository;
		this.loanRepository = loanRepository;
		this.loanEmiScheduleRepository = loanEmiScheduleRepository;
	}

	public UserDashboardResponse getDashboard(Long userId) {
		UserProfile profile = userProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("User profile not found for user: " + userId));

		List<com.microfinance.loan.user.entity.LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
		long totalApplications = applications.size();
		long pendingApplications = applications.stream().filter(a -> a.getStatus() == LoanStatus.PENDING || a.getStatus() == LoanStatus.UNDER_REVIEW || a.getStatus() == LoanStatus.PENDING_MANAGER_APPROVAL).count();
		long approvedApplications = applications.stream().filter(a -> a.getStatus() == LoanStatus.APPROVED).count();
		long disbursedApplications = applications.stream().filter(a -> a.getStatus() == LoanStatus.DISBURSED).count();

		List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
		int totalEmis = 0;
		int paidEmis = 0;
		int overdueEmis = 0;
		double outstandingPrincipal = 0d;

		for (Loan loan : loans) {
			List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId());
			totalEmis += schedule.size();
			paidEmis += (int) schedule.stream().filter(emi -> "PAID".equalsIgnoreCase(emi.getEmiStatus())).count();
			overdueEmis += (int) schedule.stream().filter(emi -> "OVERDUE".equalsIgnoreCase(emi.getEmiStatus())).count();
			outstandingPrincipal += loan.getOutstandingPrincipal() == null ? 0d : loan.getOutstandingPrincipal();
		}

		return UserDashboardResponse.builder()
				.userId(userId)
				.isHome(profile.getUsers().getIsHome())
				.kycStatus(profile.getKycStatus())
				.totalApplications(totalApplications)
				.pendingApplications(pendingApplications)
				.approvedApplications(approvedApplications)
				.disbursedApplications(disbursedApplications)
				.totalLoans((long) loans.size())
				.totalEmis(totalEmis)
				.paidEmis(paidEmis)
				.overdueEmis(overdueEmis)
				.totalOutstandingPrincipal(Math.round(outstandingPrincipal * 100d) / 100d)
				.build();
	}
}

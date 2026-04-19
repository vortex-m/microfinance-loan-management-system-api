package com.microfinance.loan.loan.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.entity.LoanStatusHistory;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.loan.repository.LoanStatusHistoryRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

	private static final double BASE_RATE = 0.06d;
	private static final double MIN_RATE = 0.015d;

	private final LoanRepository loanRepository;
	private final LoanEmiScheduleRepository loanEmiScheduleRepository;
	private final LoanStatusHistoryRepository loanStatusHistoryRepository;

	public LoanService(LoanRepository loanRepository,
					   LoanEmiScheduleRepository loanEmiScheduleRepository,
					   LoanStatusHistoryRepository loanStatusHistoryRepository) {
		this.loanRepository = loanRepository;
		this.loanEmiScheduleRepository = loanEmiScheduleRepository;
		this.loanStatusHistoryRepository = loanStatusHistoryRepository;
	}

	@Transactional
	public Loan createLoanFromApplication(LoanApplication application,
										  Users officer,
										  Users manager,
										  Users assignedAgent) {
		if (application.getApprovedAmount() == null || application.getApprovedAmount() <= 0) {
			throw new IllegalArgumentException("Approved amount is required before loan booking");
		}

		int tenureMonths = application.getTenureMonths();
		if (tenureMonths < 1 || tenureMonths > 36) {
			throw new IllegalArgumentException("Tenure must be between 1 and 36 months");
		}

		double annualRate = resolveAnnualRate(tenureMonths);
		double monthlyRate = annualRate / 12.0d;
		double principal = application.getApprovedAmount();
		double emiAmount = calculateEmi(principal, monthlyRate, tenureMonths);
		double totalAmountPayable = round(emiAmount * tenureMonths);
		double totalInterest = round(totalAmountPayable - principal);

		LocalDate disbursementDate = LocalDate.now();
		LocalDate firstEmiDate = disbursementDate.plusMonths(1);
		LocalDate lastEmiDate = firstEmiDate.plusMonths(tenureMonths - 1L);

		Loan loan = Loan.builder()
				.loanNumber(generateLoanNumber())
				.loanApplication(application)
				.user(application.getUser())
				.approvedBy(manager)
				.officerApprovedBy(officer)
				.officerApprovedAt(application.getUpdatedAt())
				.managerApprovedBy(manager)
				.managerApprovedAt(LocalDateTime.now())
				.verifiedByAgent(assignedAgent)
				.principalAmount(principal)
				.interestRate(round(annualRate * 100.0d))
				.interestType("REDUCING_BALANCE")
				.tenureMonths(tenureMonths)
				.emiAmount(emiAmount)
				.processingFee(0d)
				.totalInterestPayable(totalInterest)
				.totalAmountPayable(totalAmountPayable)
				.loanPurpose(application.getLoanPurpose())
				.disbursalMode(application.getDisbursalMode())
				.loanStatus(LoanStatus.APPROVED)
				.totalEmis(tenureMonths)
				.emisPaid(0)
				.emisPending(tenureMonths)
				.emisOverdue(0)
				.totalPaidAmount(0d)
				.outstandingPrincipal(principal)
				.outstandingInterest(totalInterest)
				.totalPenaltyCharged(0d)
				.totalPenaltyPaid(0d)
				.disbursementDate(disbursementDate)
				.firstEmiDate(firstEmiDate)
				.lastEmiDate(lastEmiDate)
				.disbursalBankName(application.getDisbursalBankName())
				.disbursalBankAccount(application.getDisbursalBankAccount())
				.disbursalIfscCode(application.getDisbursalIfscCode())
				.cashHandoverAgent(assignedAgent)
				.build();

		Loan savedLoan = loanRepository.save(loan);
		createEmiSchedule(savedLoan);
		saveStatusHistory(savedLoan, LoanStatus.PENDING_MANAGER_APPROVAL, LoanStatus.APPROVED, manager, "Loan booked after manager approval");
		return savedLoan;
	}

	@Transactional
	public void markBankDisbursed(Loan loan, String transactionReference, Users changedBy) {
		LoanStatus previousStatus = loan.getLoanStatus();
		activateEmiTimelineFromDisbursal(loan);
		loan.setDisbursalTransactionRef(transactionReference);
		loan.setDisbursedAt(LocalDateTime.now());
		loan.setLoanStatus(LoanStatus.DISBURSED);
		Loan saved = loanRepository.save(loan);
		saveStatusHistory(saved, previousStatus, LoanStatus.DISBURSED, changedBy, "Bank disbursal confirmed");
	}

	@Transactional
	public void markCashDisbursed(Loan loan, Users cashAgent) {
		LoanStatus previousStatus = loan.getLoanStatus();
		activateEmiTimelineFromDisbursal(loan);
		loan.setCashHandoverAgent(cashAgent);
		loan.setCashHandedOverAt(LocalDateTime.now());
		loan.setDisbursedAt(LocalDateTime.now());
		loan.setLoanStatus(LoanStatus.DISBURSED);
		Loan saved = loanRepository.save(loan);
		saveStatusHistory(saved, previousStatus, LoanStatus.DISBURSED, cashAgent, "Cash disbursal completed via OTP");
	}

	private void createEmiSchedule(Loan loan) {
		int tenure = loan.getTenureMonths();
		double monthlyRate = (loan.getInterestRate() / 100.0d) / 12.0d;
		double principalOutstanding = loan.getPrincipalAmount();

		List<LoanEmiSchedule> rows = new ArrayList<>();
		for (int i = 1; i <= tenure; i++) {
			double interestComponent = round(principalOutstanding * monthlyRate);
			double principalComponent = round(loan.getEmiAmount() - interestComponent);
			if (i == tenure) {
				principalComponent = round(principalOutstanding);
			}
			principalOutstanding = round(Math.max(0d, principalOutstanding - principalComponent));

			rows.add(LoanEmiSchedule.builder()
					.loan(loan)
					.emiNumber(i)
					.dueDate(loan.getFirstEmiDate().plusMonths(i - 1L))
					.emiAmount(loan.getEmiAmount())
					.principalComponent(principalComponent)
					.interestComponent(interestComponent)
					.outstandingPrincipal(principalOutstanding)
					.build());
		}
		loanEmiScheduleRepository.saveAll(rows);
	}

	private double resolveAnnualRate(int months) {
		double rate = BASE_RATE - ((months / 3.0d) * 0.01d);
		return Math.max(rate, MIN_RATE);
	}

	private double calculateEmi(double principal, double monthlyRate, int months) {
		if (monthlyRate == 0d) {
			return round(principal / months);
		}
		double factor = Math.pow(1 + monthlyRate, months);
		double emi = principal * monthlyRate * factor / (factor - 1);
		return round(emi);
	}

	private String generateLoanNumber() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "LN-" + ts + "-" + suffix;
	}

	private void saveStatusHistory(Loan loan,
								   LoanStatus previousStatus,
								   LoanStatus newStatus,
								   Users changedBy,
								   String reason) {
		LoanStatus effectivePrevious = previousStatus != null ? previousStatus : newStatus;
		LoanStatusHistory history = LoanStatusHistory.builder()
				.loan(loan)
				.previousStatus(effectivePrevious)
				.newStatus(newStatus)
				.changedBy(changedBy)
				.changedByRole(changedBy != null && changedBy.getRole() != null ? changedBy.getRole().name() : "SYSTEM")
				.changeReason(reason)
				.remarks(reason)
				.isSystemGenerated(false)
				.build();
		loanStatusHistoryRepository.save(history);
	}

	private void activateEmiTimelineFromDisbursal(Loan loan) {
		LocalDate disbursementDate = LocalDate.now();
		LocalDate firstEmiDate = disbursementDate.plusMonths(1);
		LocalDate lastEmiDate = firstEmiDate.plusMonths(loan.getTenureMonths() - 1L);

		loan.setDisbursementDate(disbursementDate);
		loan.setFirstEmiDate(firstEmiDate);
		loan.setLastEmiDate(lastEmiDate);

		List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId());
		for (LoanEmiSchedule emi : schedule) {
			emi.setDueDate(firstEmiDate.plusMonths(emi.getEmiNumber() - 1L));
		}
		loanEmiScheduleRepository.saveAll(schedule);
	}

	private double round(double value) {
		return Math.round(value * 100.0d) / 100.0d;
	}
}

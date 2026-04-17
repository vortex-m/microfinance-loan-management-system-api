package com.microfinance.loan.user.service;

import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.PaymentStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.user.dto.request.EmiPayRequest;
import com.microfinance.loan.user.dto.response.EmiScheduleResponse;
import com.microfinance.loan.user.dto.response.PaymentHistoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class UserPaymentService {

	private static final double EPSILON = 0.0001d;

	private final LoanRepository loanRepository;
	private final LoanEmiScheduleRepository loanEmiScheduleRepository;
	private final TransactionRepository transactionRepository;

	public UserPaymentService(LoanRepository loanRepository,
							  LoanEmiScheduleRepository loanEmiScheduleRepository,
							  TransactionRepository transactionRepository) {
		this.loanRepository = loanRepository;
		this.loanEmiScheduleRepository = loanEmiScheduleRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public PaymentHistoryResponse.PaymentItem payEmi(Long userId, EmiPayRequest request) {
		Loan loan = loanRepository.findById(request.getLoanId())
				.orElseThrow(() -> new IllegalArgumentException("Loan not found: " + request.getLoanId()));

		if (!loan.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("You can pay EMI only for your own loan");
		}
		if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
			throw new IllegalArgumentException("EMI can be paid only after loan is DISBURSED");
		}

		if ("CASH".equalsIgnoreCase(request.getPaymentMode())) {
			throw new IllegalArgumentException("Cash EMI collection must be verified through agent cash-collection OTP flow");
		}

		LoanEmiSchedule emi = loanEmiScheduleRepository.findById(request.getEmiScheduleId())
				.orElseThrow(() -> new IllegalArgumentException("EMI schedule not found: " + request.getEmiScheduleId()));
		if (!emi.getLoan().getId().equals(loan.getId())) {
			throw new IllegalArgumentException("EMI schedule does not belong to this loan");
		}
		if ("PAID".equalsIgnoreCase(emi.getEmiStatus())) {
			throw new IllegalArgumentException("EMI is already paid");
		}

		double payAmount = request.getPaymentAmount();
		double dueAmount = round(Math.max(0d,
				safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount()) - safe(emi.getPartialPaidAmount())));
		if (payAmount <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than zero");
		}
		if (payAmount > dueAmount + EPSILON) {
			throw new IllegalArgumentException("Payment amount cannot exceed EMI due amount");
		}

		double penaltyPaid = 0d;
		double principalPaid = 0d;
		double interestPaid = 0d;
		String emiStatus;
		double updatedPartial = round(safe(emi.getPartialPaidAmount()) + payAmount);
		if (payAmount + EPSILON < dueAmount) {
			emiStatus = "PARTIALLY_PAID";
			emi.setPartialPaidAmount(updatedPartial);
			emi.setRemainingAmount(round((safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount())) - updatedPartial));
			principalPaid = round((payAmount / dueAmount) * emi.getPrincipalComponent());
			interestPaid = round((payAmount / dueAmount) * emi.getInterestComponent());
			emi.setPaidAmount(0d);
			emi.setPaidDate(null);
		} else {
			emiStatus = "PAID";
			principalPaid = round((payAmount / dueAmount) * emi.getPrincipalComponent());
			interestPaid = round((payAmount / dueAmount) * emi.getInterestComponent());
			penaltyPaid = round((payAmount / dueAmount) * safe(emi.getPenaltyAmount()));
			emi.setPartialPaidAmount(updatedPartial);
			emi.setPaidAmount(round(safe(emi.getEmiAmount()) + safe(emi.getPenaltyAmount())));
			emi.setPaidDate(LocalDate.now());
			emi.setRemainingAmount(0d);
		}

		String paymentReference = (request.getPaymentReference() == null || request.getPaymentReference().trim().isEmpty())
				? generatePaymentReference()
				: request.getPaymentReference().trim();

		transactionRepository.findByPaymentReference(paymentReference).ifPresent(existing -> {
			throw new IllegalArgumentException("Payment reference already exists");
		});

		Payment payment = Payment.builder()
				.paymentNumber(generatePaymentNumber())
				.loan(loan)
				.emiSchedule(emi)
				.user(loan.getUser())
				.totalPaidAmount(payAmount)
				.principalPaid(principalPaid)
				.interestPaid(interestPaid)
				.penaltyPaid(penaltyPaid)
				.paymentMode(request.getPaymentMode().trim().toUpperCase())
				.gatewayOrderId(request.getGatewayOrderId())
				.paymentReference(paymentReference)
				.paymentStatus(PaymentStatus.SUCCESS)
				.gatewayStatus("SUCCESS")
				.successAt(LocalDateTime.now())
				.build();

		transactionRepository.save(payment);

		emi.setPaymentReference(paymentReference);
		emi.setEmiStatus(emiStatus);
		loanEmiScheduleRepository.save(emi);

		updateLoanAggregates(loan);

		return toPaymentItem(payment, emi);
	}

	@Transactional(readOnly = true)
	public EmiScheduleResponse getEmiSchedule(Long userId, Long loanId) {
		Loan loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
		if (!loan.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("You can view EMI schedule only for your own loan");
		}

		List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loanId);

		return EmiScheduleResponse.builder()
				.loanNumber(loan.getLoanNumber())
				.principalAmount(loan.getPrincipalAmount())
				.interestRate(loan.getInterestRate())
				.totalEmis(loan.getTotalEmis())
				.emiAmount(loan.getEmiAmount())
				.schedule(schedule.stream().map(emi -> EmiScheduleResponse.EmiItem.builder()
						.emiScheduleId(emi.getId())
						.emiNumber(emi.getEmiNumber())
						.dueDate(emi.getDueDate())
						.emiAmount(emi.getEmiAmount())
						.principalComponent(emi.getPrincipalComponent())
						.interestComponent(emi.getInterestComponent())
						.outstandingPrincipal(emi.getOutstandingPrincipal())
						.emiStatus(emi.getEmiStatus())
						.paidAmount(emi.getPaidAmount())
						.paidDate(emi.getPaidDate())
						.penaltyAmount(emi.getPenaltyAmount())
						.daysOverdue(emi.getDaysOverdue())
						.build()).toList())
				.build();
	}

	@Transactional(readOnly = true)
	public PaymentHistoryResponse getPaymentHistory(Long userId) {
		List<PaymentHistoryResponse.PaymentItem> items = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(payment -> toPaymentItem(payment, payment.getEmiSchedule()))
				.toList();

		return PaymentHistoryResponse.builder()
				.payments(items)
				.build();
	}

	private void updateLoanAggregates(Loan loan) {
		List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId());

		long paidCount = schedule.stream().filter(emi -> "PAID".equalsIgnoreCase(emi.getEmiStatus())).count();
		long partialCount = schedule.stream().filter(emi -> "PARTIALLY_PAID".equalsIgnoreCase(emi.getEmiStatus())).count();
		long overdueCount = schedule.stream().filter(emi -> "OVERDUE".equalsIgnoreCase(emi.getEmiStatus())).count();

		double totalPaid = schedule.stream().mapToDouble(emi -> safe(emi.getPaidAmount()) + safe(emi.getPartialPaidAmount())).sum();

		loan.setEmisPaid((int) paidCount);
		loan.setEmisPending((int) (loan.getTotalEmis() - paidCount));
		loan.setEmisOverdue((int) overdueCount);
		loan.setTotalPaidAmount(round(totalPaid));

		double outstandingPrincipal = schedule.stream()
				.filter(emi -> !"PAID".equalsIgnoreCase(emi.getEmiStatus()))
				.mapToDouble(emi -> safe(emi.getPrincipalComponent()))
				.sum();
		loan.setOutstandingPrincipal(round(Math.max(0d, outstandingPrincipal)));

		if (paidCount == loan.getTotalEmis()) {
			loan.setLoanStatus(LoanStatus.CLOSED);
			loan.setActualClosureDate(LocalDate.now());
			loan.setClosedAt(LocalDateTime.now());
		} else if (loan.getLoanStatus() == LoanStatus.APPROVED || loan.getLoanStatus() == LoanStatus.CLOSED) {
			loan.setLoanStatus(LoanStatus.DISBURSED);
		}

		if (partialCount > 0 && overdueCount == 0) {
			// Keep loan active but do not mark as overdue yet.
			loan.setIsNpa(false);
		}

		loanRepository.save(loan);
	}

	private PaymentHistoryResponse.PaymentItem toPaymentItem(Payment payment, LoanEmiSchedule emi) {
		return PaymentHistoryResponse.PaymentItem.builder()
				.paymentId(payment.getId())
				.paymentNumber(payment.getPaymentNumber())
				.loanNumber(payment.getLoan().getLoanNumber())
				.emiNumber(emi != null ? emi.getEmiNumber() : null)
				.totalPaidAmount(payment.getTotalPaidAmount())
				.principalPaid(payment.getPrincipalPaid())
				.interestPaid(payment.getInterestPaid())
				.penaltyPaid(payment.getPenaltyPaid())
				.paymentMode(payment.getPaymentMode())
				.paymentStatus(payment.getPaymentStatus())
				.gatewayTransactionId(payment.getGatewayTransactionId())
				.paymentReference(payment.getPaymentReference())
				.cashSettlementStatus(payment.getSettlementStatus())
				.cashVerifiedAt(payment.getVerifiedAt())
				.settledAt(payment.getSettledAt())
				.paidAt(payment.getSuccessAt())
				.receiptNumber(payment.getPaymentNumber())
				.build();
	}

	private double safe(Double value) {
		return value == null ? 0d : value;
	}

	private double round(double value) {
		return Math.round(value * 100d) / 100d;
	}

	private String generatePaymentReference() {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return "EMI-" + ts + "-" + suffix;
	}

	private String generatePaymentNumber() {
		return "PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
				+ "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
	}
}

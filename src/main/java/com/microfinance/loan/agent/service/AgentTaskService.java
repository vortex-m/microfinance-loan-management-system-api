package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.CashDisbursalOtpGenerateRequest;
import com.microfinance.loan.agent.dto.request.CashDisbursalOtpVerifyRequest;
import com.microfinance.loan.agent.dto.request.CashOtpRequest;
import com.microfinance.loan.agent.dto.request.CashOtpVerifyRequest;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.agent.dto.response.CashOtpResponse;
import com.microfinance.loan.agent.entity.CashCollectionOtp;
import com.microfinance.loan.agent.repository.CashCollectionOtpRepository;
import com.microfinance.loan.common.enums.CashOtpStatus;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.PaymentStatus;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import com.microfinance.loan.loan.repository.LoanEmiScheduleRepository;
import com.microfinance.loan.loan.repository.LoanRepository;
import com.microfinance.loan.loan.service.LoanService;
import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AgentTaskService {

    private static final int OTP_MAX_ATTEMPTS = 3;

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final LoanEmiScheduleRepository loanEmiScheduleRepository;
    private final CashCollectionOtpRepository cashCollectionOtpRepository;
    private final TransactionRepository transactionRepository;
    private final LoanService loanService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AgentTaskService(LoanApplicationRepository loanApplicationRepository,
                            LoanRepository loanRepository,
                            LoanEmiScheduleRepository loanEmiScheduleRepository,
                            CashCollectionOtpRepository cashCollectionOtpRepository,
                            TransactionRepository transactionRepository,
                            LoanService loanService,
                            PasswordEncoder passwordEncoder,
                            MailService mailService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.loanEmiScheduleRepository = loanEmiScheduleRepository;
        this.cashCollectionOtpRepository = cashCollectionOtpRepository;
        this.transactionRepository = transactionRepository;
        this.loanService = loanService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public CashDisbursalOtpResponse generateCashDisbursalOtp(Long agentId, CashDisbursalOtpGenerateRequest request) {
        LoanApplication application = loanApplicationRepository
                .findByIdAndAssignedAgentId(request.getLoanApplicationId(), agentId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not assigned to this agent."));

        validateCashOtpEligibility(application);

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        application.setCashDisbursalOtpHash(passwordEncoder.encode(otp));
        application.setCashDisbursalOtpStatus(CashOtpStatus.ACTIVE);
        application.setCashDisbursalOtpAttempts(0);
        application.setCashDisbursalOtpRequestedAt(LocalDateTime.now());
        application.setCashDisbursalOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        application.setCashDisbursalOtpVerifiedAt(null);

        loanApplicationRepository.save(application);

        if (application.getUser().getEmail() != null) {
            mailService.sendCashDisbursalOtp(
                    application.getUser().getEmail(),
                    application.getUser().getName(),
                    otp,
                    application.getApplicationNumber()
            );
        }

        return CashDisbursalOtpResponse.builder()
                .loanApplicationId(application.getId())
                .otpStatus(application.getCashDisbursalOtpStatus())
                .attempts(application.getCashDisbursalOtpAttempts())
                .expiresAt(application.getCashDisbursalOtpExpiresAt())
                .otp(null)
                .message("Cash disbursal OTP generated and sent to user email.")
                .build();
    }

    @Transactional
    public CashDisbursalOtpResponse verifyCashDisbursalOtp(Long agentId, CashDisbursalOtpVerifyRequest request) {
        LoanApplication application = loanApplicationRepository
                .findByIdAndAssignedAgentId(request.getLoanApplicationId(), agentId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not assigned to this agent."));

        if (application.getCashDisbursalOtpStatus() != CashOtpStatus.ACTIVE || application.getCashDisbursalOtpHash() == null) {
            throw new IllegalArgumentException("No active OTP found for this loan application.");
        }

        if (application.getCashDisbursalOtpExpiresAt() == null || LocalDateTime.now().isAfter(application.getCashDisbursalOtpExpiresAt())) {
            application.setCashDisbursalOtpStatus(CashOtpStatus.EXPIRED);
            loanApplicationRepository.save(application);
            throw new IllegalArgumentException("OTP expired. Please generate a new OTP.");
        }

        if (passwordEncoder.matches(request.getOtp(), application.getCashDisbursalOtpHash())) {
            application.setCashDisbursalOtpStatus(CashOtpStatus.USED);
            application.setCashDisbursalOtpVerifiedAt(LocalDateTime.now());
            application.setDisbursedAt(LocalDateTime.now());
            application.setDisbursalReference("CASH-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            application.setStatus(LoanStatus.DISBURSED);
            loanApplicationRepository.save(application);

            Loan bookedLoan = loanRepository.findByLoanApplicationId(application.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Booked loan not found for application: " + application.getId()));
            loanService.markCashDisbursed(bookedLoan, application.getAssignedAgent());

            return CashDisbursalOtpResponse.builder()
                    .loanApplicationId(application.getId())
                    .otpStatus(application.getCashDisbursalOtpStatus())
                    .attempts(application.getCashDisbursalOtpAttempts())
                    .expiresAt(application.getCashDisbursalOtpExpiresAt())
                    .verifiedAt(application.getCashDisbursalOtpVerifiedAt())
                    .message("OTP verified and cash disbursal marked successful.")
                    .build();
        }

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

    private void validateCashOtpEligibility(LoanApplication application) {
        if (application.getDisbursalMode() != DisbursalMode.CASH) {
            throw new IllegalArgumentException("OTP cash disbursal is allowed only for CASH mode loans.");
        }
        if (application.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Loan must be APPROVED before cash OTP generation.");
        }
    }

    @Transactional
    public CashOtpResponse generateCollectionOtp(Long agentId, CashOtpRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + request.getLoanId()));

        if (loan.getVerifiedByAgent() == null || !loan.getVerifiedByAgent().getId().equals(agentId)) {
            throw new IllegalArgumentException("Loan is not assigned to this agent for collection");
        }
            if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
              throw new IllegalArgumentException("Collection is allowed only after loan is DISBURSED");
        }

        LoanEmiSchedule emi = loanEmiScheduleRepository.findById(request.getEmiScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("EMI schedule not found: " + request.getEmiScheduleId()));
        if (!emi.getLoan().getId().equals(loan.getId())) {
            throw new IllegalArgumentException("EMI does not belong to this loan");
        }
        if ("PAID".equalsIgnoreCase(emi.getEmiStatus())) {
            throw new IllegalArgumentException("EMI is already paid");
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        CashCollectionOtp otpRecord = CashCollectionOtp.builder()
                .loan(loan)
                .emiSchedule(emi)
                .user(loan.getUser())
                .agent(loan.getVerifiedByAgent())
                .otpHash(passwordEncoder.encode(otp))
                .otpStatus(CashOtpStatus.ACTIVE)
                .attemptCount(0)
                .maxAttempts(OTP_MAX_ATTEMPTS)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        CashCollectionOtp savedOtp = cashCollectionOtpRepository.save(otpRecord);

        if (loan.getUser().getEmail() != null) {
            mailService.sendCashDisbursalOtp(
                    loan.getUser().getEmail(),
                    loan.getUser().getName(),
                    otp,
                    loan.getLoanNumber()
            );
        }

        return CashOtpResponse.builder()
                .otpId(savedOtp.getId())
                .taskId(request.getTaskId())
                .otpStatus(savedOtp.getOtpStatus())
                .expiresAt(savedOtp.getExpiresAt())
                .attemptCount(savedOtp.getAttemptCount())
                .maxAttempts(savedOtp.getMaxAttempts())
                .build();
    }

    @Transactional
    public CashOtpResponse verifyCollectionOtp(Long agentId, CashOtpVerifyRequest request) {
        CashCollectionOtp otpRecord = cashCollectionOtpRepository.findByIdAndAgentId(request.getOtpId(), agentId)
                .orElseThrow(() -> new IllegalArgumentException("Collection OTP record not found for this agent"));

        if (otpRecord.getOtpStatus() != CashOtpStatus.ACTIVE) {
            throw new IllegalArgumentException("OTP is not active");
        }
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            otpRecord.setOtpStatus(CashOtpStatus.EXPIRED);
            cashCollectionOtpRepository.save(otpRecord);
            throw new IllegalArgumentException("OTP expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpRecord.getOtpHash())) {
            int attempts = otpRecord.getAttemptCount() == null ? 0 : otpRecord.getAttemptCount();
            attempts++;
            otpRecord.setAttemptCount(attempts);
            if (attempts >= otpRecord.getMaxAttempts()) {
                otpRecord.setOtpStatus(CashOtpStatus.BLOCKED);
            }
            cashCollectionOtpRepository.save(otpRecord);
            throw new IllegalArgumentException(attempts >= otpRecord.getMaxAttempts()
                    ? "OTP blocked due to too many invalid attempts"
                    : "Invalid OTP");
        }

        otpRecord.setOtpStatus(CashOtpStatus.USED);
        otpRecord.setVerifiedAt(LocalDateTime.now());
        cashCollectionOtpRepository.save(otpRecord);

        LoanEmiSchedule emi = otpRecord.getEmiSchedule();
        Loan loan = otpRecord.getLoan();

        String paymentReference = "CASH-EMI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Payment payment = Payment.builder()
                .paymentNumber("PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .loan(loan)
                .emiSchedule(emi)
                .user(loan.getUser())
                .totalPaidAmount(emi.getEmiAmount() + safe(emi.getPenaltyAmount()))
                .principalPaid(emi.getPrincipalComponent())
                .interestPaid(emi.getInterestComponent())
                .penaltyPaid(safe(emi.getPenaltyAmount()))
                .paymentMode("CASH")
                .paymentReference(paymentReference)
                .paymentStatus(PaymentStatus.SUCCESS)
                .gatewayStatus("SUCCESS")
                .verifiedBy(otpRecord.getAgent())
                .cashCollectionOtp(otpRecord)
                .verifiedAt(LocalDateTime.now())
                .successAt(LocalDateTime.now())
                .build();
        transactionRepository.save(payment);

        emi.setEmiStatus("PAID");
        emi.setPaidAmount(payment.getTotalPaidAmount());
        emi.setPaidDate(LocalDateTime.now().toLocalDate());
        emi.setPaymentReference(paymentReference);
        emi.setRemainingAmount(0d);
        loanEmiScheduleRepository.save(emi);

        syncLoanAfterCollection(loan);

        return CashOtpResponse.builder()
                .otpId(otpRecord.getId())
                .otpStatus(otpRecord.getOtpStatus())
                .expiresAt(otpRecord.getExpiresAt())
                .attemptCount(otpRecord.getAttemptCount())
                .maxAttempts(otpRecord.getMaxAttempts())
                .build();
    }

    private void syncLoanAfterCollection(Loan loan) {
        List<LoanEmiSchedule> schedule = loanEmiScheduleRepository.findByLoanIdOrderByEmiNumberAsc(loan.getId());
        int paid = (int) schedule.stream().filter(emi -> "PAID".equalsIgnoreCase(emi.getEmiStatus())).count();
        int overdue = (int) schedule.stream().filter(emi -> "OVERDUE".equalsIgnoreCase(emi.getEmiStatus())).count();
        double totalPaid = schedule.stream().mapToDouble(emi -> safe(emi.getPaidAmount())).sum();
        double outstanding = schedule.stream()
                .filter(emi -> !"PAID".equalsIgnoreCase(emi.getEmiStatus()))
                .mapToDouble(emi -> safe(emi.getPrincipalComponent()))
                .sum();

        loan.setEmisPaid(paid);
        loan.setEmisPending(Math.max(0, loan.getTotalEmis() - paid));
        loan.setEmisOverdue(overdue);
        loan.setTotalPaidAmount(Math.round(totalPaid * 100d) / 100d);
        loan.setOutstandingPrincipal(Math.round(outstanding * 100d) / 100d);

        if (paid == loan.getTotalEmis()) {
            loan.setLoanStatus(LoanStatus.CLOSED);
            loan.setActualClosureDate(java.time.LocalDate.now());
            loan.setClosedAt(LocalDateTime.now());
        }

        loanRepository.save(loan);
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }
}

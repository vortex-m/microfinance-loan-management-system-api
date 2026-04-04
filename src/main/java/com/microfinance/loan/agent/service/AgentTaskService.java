package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.CashDisbursalOtpGenerateRequest;
import com.microfinance.loan.agent.dto.request.CashDisbursalOtpVerifyRequest;
import com.microfinance.loan.agent.dto.response.CashDisbursalOtpResponse;
import com.microfinance.loan.common.enums.CashOtpStatus;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.user.entity.LoanApplication;
import com.microfinance.loan.user.repository.LoanApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AgentTaskService {

    private static final int OTP_MAX_ATTEMPTS = 3;

    private final LoanApplicationRepository loanApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AgentTaskService(LoanApplicationRepository loanApplicationRepository,
                            PasswordEncoder passwordEncoder,
                            MailService mailService) {
        this.loanApplicationRepository = loanApplicationRepository;
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
                .otp(otp)
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
}

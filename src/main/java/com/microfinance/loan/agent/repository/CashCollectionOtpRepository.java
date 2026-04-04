package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.CashCollectionOtp;
import com.microfinance.loan.common.enums.CashOtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashCollectionOtpRepository extends JpaRepository<CashCollectionOtp, Long> {
    Optional<CashCollectionOtp> findTopByLoanIdAndEmiScheduleIdAndOtpStatusOrderByCreatedAtDesc(
            Long loanId,
            Long emiScheduleId,
            CashOtpStatus otpStatus
    );
}


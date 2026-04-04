package com.microfinance.loan.user.repository;

import com.microfinance.loan.user.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByIdAndUserId(Long loanApplicationId, Long userId);
    Optional<LoanApplication> findByIdAndAssignedAgentId(Long loanApplicationId, Long agentId);
}

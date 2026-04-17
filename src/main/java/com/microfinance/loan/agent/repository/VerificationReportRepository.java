package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.VerificationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationReportRepository extends JpaRepository<VerificationReport, Long> {
	Optional<VerificationReport> findByTaskId(Long taskId);

	Optional<VerificationReport> findByTaskIdAndAgentId(Long taskId, Long agentId);

	Optional<VerificationReport> findTopByLoanApplicationIdOrderByCreatedAtDesc(Long loanApplicationId);
}

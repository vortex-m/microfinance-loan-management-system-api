package com.microfinance.loan.agent.repository;

import com.microfinance.loan.agent.entity.VerificationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationReportRepository extends JpaRepository<VerificationReport, Long> {
}

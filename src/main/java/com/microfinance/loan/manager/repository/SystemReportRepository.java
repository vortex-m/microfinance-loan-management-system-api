package com.microfinance.loan.manager.repository;

import com.microfinance.loan.common.enums.ReportStatus;
import com.microfinance.loan.manager.entity.SystemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemReportRepository extends JpaRepository<SystemReport, Long> {
    List<SystemReport> findByReportStatusOrderByCreatedAtDesc(ReportStatus reportStatus);
}


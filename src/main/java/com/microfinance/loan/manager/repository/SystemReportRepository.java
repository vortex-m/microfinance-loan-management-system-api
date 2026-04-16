package com.microfinance.loan.manager.repository;

import com.microfinance.loan.common.enums.ReportStatus;
import com.microfinance.loan.manager.entity.SystemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemReportRepository extends JpaRepository<SystemReport, Long> {
    List<SystemReport> findByReportStatusOrderByCreatedAtDesc(ReportStatus reportStatus);

    @Query("select sr from SystemReport sr " +
            "join ManagerProfile mp on mp.users.id = sr.generatedBy.id " +
            "join mp.branchProfile bp " +
            "where bp.branchCode = :branchCode " +
            "order by sr.createdAt desc")
    List<SystemReport> findByBranchCodeOrderByCreatedAtDesc(@Param("branchCode") String branchCode);

    @Query("select sr from SystemReport sr " +
            "join ManagerProfile mp on mp.users.id = sr.generatedBy.id " +
            "join mp.branchProfile bp " +
            "where sr.id = :reportId and bp.branchCode = :branchCode")
    Optional<SystemReport> findByIdAndBranchCode(@Param("reportId") Long reportId,
                                                  @Param("branchCode") String branchCode);
}

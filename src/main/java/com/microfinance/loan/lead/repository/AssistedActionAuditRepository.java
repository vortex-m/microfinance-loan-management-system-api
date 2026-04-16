package com.microfinance.loan.lead.repository;

import com.microfinance.loan.lead.entity.AssistedActionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistedActionAuditRepository extends JpaRepository<AssistedActionAudit, Long> {
    List<AssistedActionAudit> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    List<AssistedActionAudit> findTop100ByOrderByCreatedAtDesc();

    List<AssistedActionAudit> findTop100ByLeadBranchProfileBranchCodeOrderByCreatedAtDesc(String branchCode);
}

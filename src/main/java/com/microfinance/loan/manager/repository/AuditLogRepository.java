package com.microfinance.loan.manager.repository;

import com.microfinance.loan.manager.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByLoanApplicationIdOrderByCreatedAtDesc(Long loanApplicationId);

    @Query("select al from AuditLog al " +
            "join al.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where la.id = :loanApplicationId and bp.branchCode = :branchCode " +
            "order by al.createdAt desc")
    List<AuditLog> findByLoanApplicationIdAndBranchCode(@Param("loanApplicationId") Long loanApplicationId,
                                                        @Param("branchCode") String branchCode);

    @Query("select al from AuditLog al " +
            "join al.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where al.id = :auditId and bp.branchCode = :branchCode")
    Optional<AuditLog> findByIdAndBranchCode(@Param("auditId") Long auditId,
                                             @Param("branchCode") String branchCode);
}

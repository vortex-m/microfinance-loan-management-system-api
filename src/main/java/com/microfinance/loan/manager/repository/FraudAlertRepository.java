package com.microfinance.loan.manager.repository;

import com.microfinance.loan.common.enums.FraudAlertStatus;
import com.microfinance.loan.manager.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByAlertStatusOrderByCreatedAtDesc(FraudAlertStatus alertStatus);

    @Query("select fa from FraudAlert fa " +
            "join fa.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where bp.branchCode = :branchCode " +
            "order by fa.createdAt desc")
    List<FraudAlert> findByBranchCodeOrderByCreatedAtDesc(@Param("branchCode") String branchCode);

    @Query("select fa from FraudAlert fa " +
            "join fa.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where bp.branchCode = :branchCode and fa.alertStatus = :status " +
            "order by fa.createdAt desc")
    List<FraudAlert> findByBranchCodeAndAlertStatusOrderByCreatedAtDesc(@Param("branchCode") String branchCode,
                                                                         @Param("status") FraudAlertStatus status);

    @Query("select fa from FraudAlert fa " +
            "join fa.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where fa.id = :alertId and bp.branchCode = :branchCode")
    Optional<FraudAlert> findByIdAndBranchCode(@Param("alertId") Long alertId,
                                               @Param("branchCode") String branchCode);

    @Query("select count(fa) from FraudAlert fa " +
            "join fa.loanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where bp.branchCode = :branchCode")
    long countByBranchCode(@Param("branchCode") String branchCode);
}

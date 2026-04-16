package com.microfinance.loan.user.repository;

import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.user.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    @EntityGraph(attributePaths = {"user", "assignedOfficer", "assignedAgent"})
    Optional<LoanApplication> findByIdAndUserId(Long loanApplicationId, Long userId);

    @EntityGraph(attributePaths = {"user", "assignedOfficer", "assignedAgent"})
    Optional<LoanApplication> findByIdAndAssignedAgentId(Long loanApplicationId, Long agentId);

    @EntityGraph(attributePaths = {"user", "assignedOfficer", "assignedAgent"})
    Optional<LoanApplication> findById(Long id);

    @EntityGraph(attributePaths = {"user", "assignedOfficer", "assignedAgent"})
    List<LoanApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select la from LoanApplication la " +
            "join fetch la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "left join fetch la.assignedOfficer " +
            "left join fetch la.assignedAgent " +
            "where bp.branchCode = :branchCode and la.status = :status " +
            "order by la.createdAt asc")
    List<LoanApplication> findByBranchCodeAndStatus(@Param("branchCode") String branchCode,
                                                    @Param("status") LoanStatus status);

    @Query("select la from LoanApplication la " +
            "join fetch la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "left join fetch la.assignedOfficer " +
            "left join fetch la.assignedAgent " +
            "where bp.branchCode = :branchCode and la.status in :statuses " +
            "order by la.createdAt asc")
    List<LoanApplication> findByBranchCodeAndStatuses(@Param("branchCode") String branchCode,
                                                      @Param("statuses") List<LoanStatus> statuses);

    @Query("select count(la) from LoanApplication la " +
            "join la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "where bp.branchCode = :branchCode and la.status in :statuses")
    long countByBranchCodeAndStatuses(@Param("branchCode") String branchCode,
                                      @Param("statuses") List<LoanStatus> statuses);

    @Query("select count(la) from LoanApplication la where la.user.id in " +
            "(select up.users.id from UserProfile up where up.branchProfile.branchCode = :branchCode)")
    long countByBranchCode(@Param("branchCode") String branchCode);

    @Query("select count(la) from LoanApplication la where la.status = :status and la.user.id in " +
            "(select up.users.id from UserProfile up where up.branchProfile.branchCode = :branchCode)")
    long countByBranchCodeAndStatus(@Param("branchCode") String branchCode, @Param("status") LoanStatus status);

    @Query("select la from LoanApplication la " +
            "join fetch la.user u " +
            "join UserProfile up on up.users.id = u.id " +
            "join up.branchProfile bp " +
            "left join fetch la.assignedOfficer " +
            "left join fetch la.assignedAgent " +
            "where bp.branchCode = :branchCode and la.assignedAgent.id = :agentUserId and la.status in :statuses")
    List<LoanApplication> findByBranchCodeAndAssignedAgentIdAndStatuses(@Param("branchCode") String branchCode,
                                                                         @Param("agentUserId") Long agentUserId,
                                                                         @Param("statuses") List<LoanStatus> statuses);

    @Query("select coalesce(sum(coalesce(la.approvedAmount, la.requestedAmount)), 0) from LoanApplication la where la.user.id in " +
            "(select up.users.id from UserProfile up where up.branchProfile.branchCode = :branchCode)")
    Double sumTotalAmountByBranchCode(@Param("branchCode") String branchCode);
}

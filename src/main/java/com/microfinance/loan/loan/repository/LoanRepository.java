package com.microfinance.loan.loan.repository;

import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
	Optional<Loan> findByLoanApplicationId(Long loanApplicationId);

	Optional<Loan> findByIdAndVerifiedByAgentId(Long loanId, Long agentUserId);

	List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);

	long countByLoanStatus(LoanStatus loanStatus);

	@Query("select count(l) from Loan l where l.loanStatus in :statuses")
	long countByLoanStatuses(@Param("statuses") List<LoanStatus> statuses);

	@Query("select coalesce(sum(coalesce(l.totalPaidAmount, 0)), 0) from Loan l " +
			"join l.loanApplication la " +
			"join la.user u " +
			"join UserProfile up on up.users.id = u.id " +
			"join up.branchProfile bp " +
			"where bp.branchCode = :branchCode")
	Double sumTotalPaidAmountByBranchCode(@Param("branchCode") String branchCode);

	@Query("select count(l) from Loan l " +
			"join l.loanApplication la " +
			"join la.user u " +
			"join UserProfile up on up.users.id = u.id " +
			"join up.branchProfile bp " +
			"where bp.branchCode = :branchCode and l.isNpa = true")
	long countNpaByBranchCode(@Param("branchCode") String branchCode);

	@Query("select l from Loan l " +
			"join l.loanApplication la " +
			"join la.user u " +
			"join UserProfile up on up.users.id = u.id " +
			"join up.branchProfile bp " +
			"where bp.branchCode = :branchCode and l.verifiedByAgent.id = :agentUserId and l.loanStatus in :statuses")
	List<Loan> findByBranchCodeAndVerifiedByAgentIdAndLoanStatusIn(@Param("branchCode") String branchCode,
																		  @Param("agentUserId") Long agentUserId,
																		  @Param("statuses") List<LoanStatus> statuses);
}

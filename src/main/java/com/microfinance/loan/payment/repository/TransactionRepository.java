package com.microfinance.loan.payment.repository;

import com.microfinance.loan.payment.entity.Payment;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Payment> findByLoanIdOrderByCreatedAtDesc(Long loanId);

	Optional<Payment> findByPaymentReference(String paymentReference);

	@Query("select coalesce(sum(p.totalPaidAmount), 0.0) from Payment p where p.verifiedBy.id = :agentUserId and p.paymentMode = 'CASH' and p.paymentStatus = com.microfinance.loan.common.enums.PaymentStatus.SUCCESS and p.settlementStatus = :settlementStatus")
	Double sumCashAmountByAgentAndSettlementStatus(@Param("agentUserId") Long agentUserId,
									   @Param("settlementStatus") CashSettlementStatus settlementStatus);

	@Query("select coalesce(sum(p.totalPaidAmount), 0.0) from Payment p join AgentProfile ap on ap.users.id = p.verifiedBy.id join ap.branchProfile bp where bp.branchCode = :branchCode and p.paymentMode = 'CASH' and p.paymentStatus = com.microfinance.loan.common.enums.PaymentStatus.SUCCESS and p.settlementStatus = :settlementStatus")
	Double sumCashAmountByBranchAndSettlementStatus(@Param("branchCode") String branchCode,
										@Param("settlementStatus") CashSettlementStatus settlementStatus);

	@Query("select p from Payment p join AgentProfile ap on ap.users.id = p.verifiedBy.id join ap.branchProfile bp where p.id in :paymentIds and bp.branchCode = :branchCode and p.paymentMode = 'CASH' and p.paymentStatus = com.microfinance.loan.common.enums.PaymentStatus.SUCCESS and p.settlementStatus = com.microfinance.loan.common.enums.CashSettlementStatus.COLLECTED_UNSETTLED")
	List<Payment> findBranchUnsettledCashPaymentsByIds(@Param("paymentIds") List<Long> paymentIds,
											 @Param("branchCode") String branchCode);
}

package com.microfinance.loan.payment.repository;

import com.microfinance.loan.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Payment> findByLoanIdOrderByCreatedAtDesc(Long loanId);

	Optional<Payment> findByPaymentReference(String paymentReference);
}

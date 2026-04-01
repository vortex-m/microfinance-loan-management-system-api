package com.microfinance.loan.user.repository;

import com.microfinance.loan.user.entity.EmiPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiPaymentRepository extends JpaRepository<EmiPayment, Long> {
}

package com.microfinance.loan.loan.repository;

import com.microfinance.loan.loan.entity.LoanStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanStatusHistoryRepository extends JpaRepository<LoanStatusHistory, Long> {
}


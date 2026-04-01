package com.microfinance.loan.manager.repository;

import com.microfinance.loan.common.enums.FraudAlertStatus;
import com.microfinance.loan.manager.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByAlertStatusOrderByCreatedAtDesc(FraudAlertStatus alertStatus);
}


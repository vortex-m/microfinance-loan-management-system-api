package com.microfinance.loan.manager.repository;

import com.microfinance.loan.manager.entity.ManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, Long> {
    Optional<ManagerProfile> findByManagerCode(String managerCode);

    boolean existsByManagerCode(String managerCode);
}


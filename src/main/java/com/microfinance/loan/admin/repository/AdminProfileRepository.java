package com.microfinance.loan.admin.repository;

import com.microfinance.loan.admin.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByUsersId(Long userId);

    boolean existsByUsersId(Long userId);
}


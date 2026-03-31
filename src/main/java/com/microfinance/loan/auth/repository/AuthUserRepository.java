package com.microfinance.loan.auth.repository;

import com.microfinance.loan.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<User, Long> {
}


package com.microfinance.loan.manager.repository;

import com.microfinance.loan.manager.entity.ManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, Long> {
    Optional<ManagerProfile> findByManagerCode(String managerCode);

    @Query("select mp from ManagerProfile mp join fetch mp.users where mp.managerCode = :managerCode")
    Optional<ManagerProfile> findByManagerCodeWithUsers(@Param("managerCode") String managerCode);

    Optional<ManagerProfile> findByUsersId(Long userId);

    boolean existsByUsersId(Long userId);
}


package com.microfinance.loan.user.repository;

import com.microfinance.loan.common.enums.UserStatus;
import com.microfinance.loan.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
  boolean existsByUsersId(Long userId);
  Optional<UserProfile> findByUsersId(Long userId);

  @Query("select up from UserProfile up join fetch up.users")
  List<UserProfile> findAllWithUsers();

  @Query("select up from UserProfile up join fetch up.users u join up.branchProfile bp where bp.branchCode = :branchCode")
  List<UserProfile> findAllWithUsersByBranchCode(@Param("branchCode") String branchCode);

  @Query("select count(up) from UserProfile up where up.branchProfile.branchCode = :branchCode")
  long countByBranchCode(@Param("branchCode") String branchCode);

  @Query("select count(up) from UserProfile up where up.branchProfile.branchCode = :branchCode and up.users.status = :status")
  long countByBranchCodeAndUserStatus(@Param("branchCode") String branchCode, @Param("status") UserStatus status);
}

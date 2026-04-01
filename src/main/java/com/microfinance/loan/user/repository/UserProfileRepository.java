package com.microfinance.loan.user.repository;

import com.microfinance.loan.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
  boolean existsByUsersId(Long userId);
  Optional<UserProfile> findByUsersId(Long userId);

  @Query("select up from UserProfile up join fetch up.users")
  List<UserProfile> findAllWithUsers();
}

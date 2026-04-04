package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.user.dto.request.UpdateProfileRequest;
import com.microfinance.loan.user.dto.response.UserProfileResponse;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserProfileResponse completeOnboarding(Long userId, UpdateProfileRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getRole() != Role.USER) {
            throw new IllegalArgumentException("Onboarding is allowed only for user role");
        }

        UserProfile profile = userProfileRepository.findByUsersId(userId)
                .orElseGet(() -> UserProfile.builder().users(user).build());

        applyUpdate(profile, request);

        boolean isOnboardingComplete = isOnboardingComplete(profile);
        user.setIsHome(isOnboardingComplete);

        UserProfile savedProfile = userProfileRepository.save(profile);
        userRepository.save(user);

        return mapToResponse(savedProfile, user);
    }

    private void applyUpdate(UserProfile profile, UpdateProfileRequest request) {
        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation().trim());
        }
        if (request.getMaritalStatus() != null) {
            profile.setMaritalStatus(request.getMaritalStatus().trim());
        }
        if(request.getFatherName() != null) {
            profile.setFatherName(request.getFatherName().trim());
        }
        if(request.getMotherName() != null) {
            profile.setMotherName(request.getMotherName().trim());
        }
        if(request.getWifeName() != null) {
            profile.setWifeName(request.getWifeName().trim());
        }
        if(request.getHusbandName() != null) {
            profile.setHusbandName(request.getHusbandName().trim());
        }
        if (request.getMonthlyIncome() != null) {
            profile.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (request.getStreet() != null) {
            profile.setStreet(request.getStreet().trim());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity().trim());
        }
        if (request.getState() != null) {
            profile.setState(request.getState().trim());
        }
        if (request.getPinCode() != null) {
            profile.setPinCode(request.getPinCode().trim());
        }
        if(request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
    }

    private boolean isOnboardingComplete(UserProfile profile) {
        return StringUtils.hasText(profile.getOccupation())
                && StringUtils.hasText(profile.getMaritalStatus())
                && StringUtils.hasText(profile.getFatherName())
                && StringUtils.hasText(profile.getMotherName())
                && profile.getMonthlyIncome() != null
                && profile.getMonthlyIncome() > 0
                && StringUtils.hasText(profile.getStreet())
                && StringUtils.hasText(profile.getCity())
                && StringUtils.hasText(profile.getState())
                && StringUtils.hasText(profile.getPinCode());
    }

    private UserProfileResponse mapToResponse(UserProfile profile, Users user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .isHome(user.getIsHome())
                .fatherName(profile.getFatherName())
                .motherName(profile.getMotherName())
                .wifeName(profile.getWifeName())
                .husbandName(profile.getHusbandName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .occupation(profile.getOccupation())
                .maritalStatus(profile.getMaritalStatus())
                .monthlyIncome(profile.getMonthlyIncome())
                .aadhaarNumber(profile.getAadhaarNumber())
                .panNumber(profile.getPanNumber())
                .kycStatus(profile.getKycStatus())
//                .kycApproved(profile.getKycApproved())
                .street(profile.getStreet())
                .city(profile.getCity())
                .state(profile.getState())
                .pinCode(profile.getPinCode())
                .creditScore(profile.getCreditScore())
                .riskScore(profile.getRiskScore())
                .scoreUpdatedAt(profile.getScoreUpdatedAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}

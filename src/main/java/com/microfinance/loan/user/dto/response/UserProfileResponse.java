package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.enums.UserStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    // From Users entity
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;

    // From UserProfile entity
    private String fatherName;
    private String motherName;
    private LocalDate dateOfBirth;
    private String gender;
    private String occupation;
    private String maritalStatus;
    private Double monthlyIncome;
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;

    // KYC
    private String aadhaarNumber;
    private String panNumber;
    private KycStatus kycStatus;

    // Address
    private String street;
    private String city;
    private String state;
    private String pincode;

    // AI Scores
    private Double creditScore;
    private Double riskScore;
    private LocalDateTime scoreUpdatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
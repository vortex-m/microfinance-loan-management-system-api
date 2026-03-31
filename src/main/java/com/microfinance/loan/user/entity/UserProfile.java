package com.microfinance.loan.user.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;


    private String fatherName;
    private String motherName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    private String gender;
    private String occupation;
    private String maritalStatus;


    private Double monthlyIncome;
    private String bankAccountNumber;
    private String bankName;
    private String ifscCode;


    @Column(unique = true)
    private String aadhaarNumber;

    @Column(unique = true)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus;


    private String street;
    private String city;
    private String state;
    private String pincode;

    // AI Score
    private Double creditScore; // 400 to 900
    private Double riskScore;   // 0.0 - 1.0
    private LocalDateTime scoreUpdatedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.kycStatus = KycStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
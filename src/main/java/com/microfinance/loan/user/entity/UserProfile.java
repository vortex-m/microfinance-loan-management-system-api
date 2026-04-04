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
    private String wifeName;
    private String husbandName;

    private LocalDate dateOfBirth;

    private String gender;
    private String occupation;
    private String maritalStatus;


    private Double monthlyIncome;


    @Column(unique = true)
    private String aadhaarNumber;

    @Column(unique = true)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus;

//    @Column(nullable = false)
//    private Boolean kycApproved;


    private String street;
    private String city;
    private String state;
    private String pinCode;

    // AI Score
    private Double creditScore;
    private Double riskScore;
    private LocalDateTime scoreUpdatedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.kycStatus = KycStatus.PENDING;
//        if (this.kycApproved == null) {
//            this.kycApproved = false;
//        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
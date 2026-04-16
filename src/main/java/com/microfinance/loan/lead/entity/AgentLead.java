package com.microfinance.loan.lead.entity;

import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.ConsentMode;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LeadStatus;
import com.microfinance.loan.common.enums.OriginChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String leadCode;

    @Column(nullable = false)
    private String fullName;

    private String phone;
    private String guardianPhone;
    private String email;

    private String village;
    private String address;

    private String fatherName;
    private String motherName;
    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String occupation;
    private Double monthlyIncome;
    private String city;
    private String state;
    private String pinCode;

    private String aadhaarNumber;
    private String panNumber;
    private String aadhaarFileUrl;
    private String panFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchProfile branchProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id", nullable = false)
    private Users agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_user_id")
    private Users assignedOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_user_id")
    private Users convertedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OriginChannel originChannel;

    @Enumerated(EnumType.STRING)
    private ConsentMode consentMode;

    private String consentText;
    private String consentProofUrl;
    private String witnessName;
    private String witnessPhone;

    private Double requestedAmount;
    private Integer tenureMonths;
    private String loanPurpose;

    @Enumerated(EnumType.STRING)
    private DisbursalMode disbursalMode;
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;

    private String officerRemarks;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LeadStatus.NEW;
        }
        if (this.originChannel == null) {
            this.originChannel = OriginChannel.AGENT_ASSISTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


package com.microfinance.loan.lead.dto.response;

import com.microfinance.loan.common.enums.ConsentMode;
import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LeadStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private Long id;
    private String leadCode;
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

    private String branchCode;
    private String branchName;
    private Long agentUserId;
    private Long assignedOfficerUserId;
    private Long convertedUserId;

    private LeadStatus status;
    private ConsentMode consentMode;
    private String consentText;
    private String consentProofUrl;
    private String witnessName;
    private String witnessPhone;

    private Double requestedAmount;
    private Integer tenureMonths;
    private String loanPurpose;
    private DisbursalMode disbursalMode;
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;

    private String officerRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


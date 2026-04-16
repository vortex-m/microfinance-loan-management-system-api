package com.microfinance.loan.lead.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadProfileUpdateRequest {
    private String fatherName;
    private String motherName;
    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String occupation;

    @Min(value = 0, message = "Monthly income must be non-negative")
    private Double monthlyIncome;

    private String address;
    private String village;
    private String city;
    private String state;
    private String pinCode;

    private String aadhaarNumber;
    private String panNumber;
}


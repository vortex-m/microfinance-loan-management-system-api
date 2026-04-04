package com.microfinance.loan.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    // Only updatable fields
    private String occupation;
    private String maritalStatus;

    @Min(value = 1, message = "Monthly income must be greater than 0")
    private Double monthlyIncome;

//    private String bankAccountNumber;
//    private String bankName;

    private LocalDate dateOfBirth;

//    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
//            message = "Invalid IFSC code format")
//    private String ifscCode;

    // Address updatable
    private String street;
    private String city;
    private String state;

    private String fatherName;
    private String motherName;
    private String wifeName;
    private String husbandName;

    @Pattern(regexp = "^[1-9][0-9]{5}$",
            message = "Invalid pincode")
    private String pinCode;
}
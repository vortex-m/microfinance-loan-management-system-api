package com.microfinance.loan.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {

    // Personal Info
    @NotBlank(message = "Father name is required")
    private String fatherName;

    private String motherName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Occupation is required")
    private String occupation;          // SALARIED, SELF_EMPLOYED, FARMER

    private String maritalStatus;       // SINGLE, MARRIED, DIVORCED

    // Financial Info
    @NotNull(message = "Monthly income is required")
    @Min(value = 1, message = "Monthly income must be greater than 0")
    private Double monthlyIncome;


    // KYC
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$",
            message = "Invalid Aadhaar number")
    private String aadhaarNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number format")
    private String panNumber;

    // Address
    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$",
            message = "Invalid pincode")
    private String pincode;
}
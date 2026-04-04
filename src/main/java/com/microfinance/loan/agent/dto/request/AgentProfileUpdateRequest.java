package com.microfinance.loan.agent.dto.request;


import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentProfileUpdateRequest {

    @NotBlank(message = "Father name is required")
    private String fatherName;

    @NotBlank(message = "Mother name is required")
    private String motherName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Marital status is required")
    private String maritalStatus;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid Aadhaar number")
    private String aadhaarNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;
}

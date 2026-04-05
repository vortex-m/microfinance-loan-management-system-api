package com.microfinance.loan.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfileUpsertRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String companyCode;
    private String legalEntityName;
    private String gstNumber;
    private String panNumber;

    @NotBlank(message = "Admin full name is required")
    private String adminFullName;

    private String designation;
    private String officePhone;
    private String website;

    private String address;
    private String city;
    private String state;
    private String country;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pinCode;
}


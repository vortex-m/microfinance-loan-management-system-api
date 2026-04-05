package com.microfinance.loan.admin.dto.response;

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
public class AdminProfileResponse {
    private Long userId;
    private String email;
    private String phone;

    private String companyName;
    private String companyCode;
    private String legalEntityName;
    private String gstNumber;
    private String panNumber;

    private String adminFullName;
    private String designation;
    private String officePhone;
    private String website;

    private String address;
    private String city;
    private String state;
    private String country;
    private String pinCode;
}


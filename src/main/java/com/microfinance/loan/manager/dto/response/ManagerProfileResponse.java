package com.microfinance.loan.manager.dto.response;

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
public class ManagerProfileResponse {
    private Long userId;
    private String managerCode;
    private String designation;
    private String department;
    private String branch;
    private String branchCode;
    private String region;
    private String regionCode;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String fathersName;
    private String mothersName;
}


package com.microfinance.loan.branch.dto.response;

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
public class BranchResponse {
    private Long id;
    private String branchCode;
    private String branchName;
    private String regionCode;
    private String regionName;
    private String city;
    private String state;
    private String address;
    private String pincode;
    private Boolean active;
}


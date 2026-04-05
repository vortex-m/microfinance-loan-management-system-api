package com.microfinance.loan.branch.dto.request;

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
public class BranchUpsertRequest {

    @NotBlank(message = "Branch code is required")
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Region code is required")
    private String regionCode;

    @NotBlank(message = "Region name is required")
    private String regionName;

    private String city;
    private String state;
    private String address;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    private Boolean active;
}


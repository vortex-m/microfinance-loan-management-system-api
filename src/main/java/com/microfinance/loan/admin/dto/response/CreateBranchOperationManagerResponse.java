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
public class CreateBranchOperationManagerResponse {
    private Long userId;
    private String managerCode;
    private String department;
    private String branchCode;
    private String email;
    private String tempPassword;
    private String message;
}


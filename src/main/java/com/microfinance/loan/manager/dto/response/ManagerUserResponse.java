package com.microfinance.loan.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerUserResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String branchCode;
}


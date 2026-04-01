package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagerUserResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
}


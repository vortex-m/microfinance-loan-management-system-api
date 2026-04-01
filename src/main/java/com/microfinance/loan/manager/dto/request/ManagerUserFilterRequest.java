package com.microfinance.loan.manager.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerUserFilterRequest {
    private String query;
    private String role;
    private Boolean activeOnly;
}


package com.microfinance.loan.manager.dto.response;

import com.microfinance.loan.common.enums.Role;
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
public class StaffCreateResponse {

    private Long userId;
    private Role role;
    private String code;
    private String email;
    private Boolean isHome;
    private String tempPassword;
    private String message;
}


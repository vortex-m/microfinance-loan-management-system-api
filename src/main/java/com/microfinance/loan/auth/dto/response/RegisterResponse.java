package com.microfinance.loan.auth.dto.response;

import com.microfinance.loan.common.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Boolean isHome;
    private Role role;

    private Boolean otpSent;
    private String message;
}
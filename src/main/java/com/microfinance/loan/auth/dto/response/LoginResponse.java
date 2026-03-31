package com.microfinance.loan.auth.dto.response;

import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;           // "Bearer"

    // User info returned on login
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Boolean isHome;
    private Role role;
    private UserStatus status;

    // OTP flow flag
    private Boolean otpRequired;        // true = OTP not yet verified
    private String message;
}
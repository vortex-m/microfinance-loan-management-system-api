package com.microfinance.loan.auth.dto.response;

import com.microfinance.loan.common.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerifyResponse {

    private Boolean verified;
    private String accessToken;
    private String refreshToken;
    private String tokenType;

    private Long userId;
    private Role role;
    private String message;
}
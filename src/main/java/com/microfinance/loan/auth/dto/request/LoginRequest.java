package com.microfinance.loan.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Login ID is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;
}
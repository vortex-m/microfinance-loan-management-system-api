package com.microfinance.loan.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
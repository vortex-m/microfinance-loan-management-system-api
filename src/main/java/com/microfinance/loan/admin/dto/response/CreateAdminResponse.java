package com.microfinance.loan.admin.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdminResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String message;
}

package com.microfinance.loan.auth.controller;

import com.microfinance.loan.auth.dto.request.LoginRequest;
import com.microfinance.loan.auth.dto.request.RegisterRequest;
import com.microfinance.loan.auth.dto.response.LoginResponse;
import com.microfinance.loan.auth.dto.response.RegisterResponse;
import com.microfinance.loan.auth.service.AuthService;
import com.microfinance.loan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User registered", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }
}

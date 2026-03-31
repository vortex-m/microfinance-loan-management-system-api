package com.microfinance.loan.auth.service;

import com.microfinance.loan.auth.dto.request.LoginRequest;
import com.microfinance.loan.auth.dto.request.RegisterRequest;
import com.microfinance.loan.auth.dto.response.LoginResponse;
import com.microfinance.loan.auth.dto.response.RegisterResponse;
import com.microfinance.loan.auth.security.JwtUtil;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   JwtUtil jwtUtil,
					   UserDetailsServiceImpl userDetailsService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
			throw new IllegalArgumentException("Email already exists");
		}
		if (userRepository.existsByPhone(request.getPhone().trim())) {
			throw new IllegalArgumentException("Phone already exists");
		}

		Users user = Users.builder()
				.name(request.getName().trim())
				.email(request.getEmail().trim().toLowerCase())
				.phone(request.getPhone().trim())
				.password(passwordEncoder.encode(request.getPassword()))
				.isHome(false)
				.role(request.getRole())
				.address(request.getAddress())
				.build();

		Users saved = userRepository.save(user);

		return RegisterResponse.builder()
				.userId(saved.getId())
				.name(saved.getName())
				.email(saved.getEmail())
				.phone(saved.getPhone())
				.isHome(saved.getIsHome() != null ? saved.getIsHome() : false)
				.role(saved.getRole())
				.otpSent(false)
				.message("Registration successful")
				.build();
	}

	public LoginResponse login(LoginRequest request) {
		String loginId = request.getLoginId().trim();
		Users user = userDetailsService.resolveUser(loginId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		String accessToken = jwtUtil.generateToken(loginId, Map.of("role", user.getRole().name(), "uid", user.getId()));

		return LoginResponse.builder()
				.accessToken(accessToken)
				.refreshToken(null)
				.tokenType("Bearer")
				.userId(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.phone(user.getPhone())
				.isHome(user.getIsHome() != null ? user.getIsHome() : false)
				.role(user.getRole())
				.status(user.getStatus())
				.otpRequired(false)
				.message(buildLoginMessage(user.getRole(), loginId))
				.build();
	}

	private String buildLoginMessage(Role role, String loginId) {
		if (role == Role.AGENT || role == Role.OFFICER || role == Role.MANAGER) {
			return "Login successful using code/email: " + loginId;
		}
		return "Login successful";
	}
}

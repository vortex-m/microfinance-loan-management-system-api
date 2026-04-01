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
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

	private final UserProfileRepository userProfileRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;
	private final ManagerProfileRepository managerProfileRepository;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   JwtUtil jwtUtil,
					   UserDetailsServiceImpl userDetailsService,
					   UserProfileRepository userProfileRepository,
					   ManagerProfileRepository managerProfileRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.userProfileRepository = userProfileRepository;
		this.managerProfileRepository = managerProfileRepository;
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
		ensureUserProfile(saved);
		ensureManagerProfile(saved);

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

	@Transactional
	public LoginResponse login(LoginRequest request) {
		String loginId = request.getLoginId().trim();
		Users user = userDetailsService.resolveUser(loginId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		updateManagerLastLogin(user);

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

	private void ensureUserProfile(Users user) {
		if(user == null || user.getRole() != Role.USER || user.getId() == null){
			return;
		}
		if(userProfileRepository.existsByUsersId(user.getId())){
			return;
		}

		UserProfile userProfile = UserProfile.builder()
				.users(user)
				.build();
		userProfileRepository.save(userProfile);
	}

	private void ensureManagerProfile(Users user) {
		if (user == null || user.getRole() != Role.MANAGER || user.getId() == null) {
			return;
		}
		if (managerProfileRepository.existsByUsersId(user.getId())) {
			return;
		}

		String managerCode = "MGR" + String.format("%05d", user.getId());
		ManagerProfile profile = ManagerProfile.builder()
				.users(user)
				.managerCode(managerCode)
				.accessLevel("STANDARD")
				.build();
		managerProfileRepository.save(profile);
	}

	private void updateManagerLastLogin(Users user) {
		if (user == null || user.getRole() != Role.MANAGER || user.getId() == null) {
			return;
		}

		ManagerProfile profile = managerProfileRepository.findByUsersId(user.getId())
				.orElseGet(() -> {
					String managerCode = "MGR" + String.format("%05d", user.getId());
					return managerProfileRepository.save(
							ManagerProfile.builder()
									.users(user)
									.managerCode(managerCode)
									.accessLevel("STANDARD")
									.build()
					);
				});

		profile.setLastLoginAt(LocalDateTime.now());
		managerProfileRepository.save(profile);
	}
}

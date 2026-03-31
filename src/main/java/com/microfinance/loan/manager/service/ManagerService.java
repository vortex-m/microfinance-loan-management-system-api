package com.microfinance.loan.manager.service;

import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentStatus;
import com.microfinance.loan.common.enums.OfficerStatus;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@Service
public class ManagerService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final AgentProfileRepository agentProfileRepository;
	private final OfficerProfileRepository officerProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;

	public ManagerService(UserRepository userRepository,
						  AgentProfileRepository agentProfileRepository,
						  OfficerProfileRepository officerProfileRepository,
						  PasswordEncoder passwordEncoder,
						  MailService mailService) {
		this.userRepository = userRepository;
		this.agentProfileRepository = agentProfileRepository;
		this.officerProfileRepository = officerProfileRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailService = mailService;
	}

	@Transactional
	public StaffCreateResponse createAgent(CreateStaffRequest request) {
		validateCommonFields(request);
		if (request.getRole() != Role.AGENT) {
			throw new IllegalArgumentException("Role must be AGENT for this endpoint");
		}

		if (agentProfileRepository.existsByAgentCode(request.getCode())) {
			throw new IllegalArgumentException("Agent code already exists: " + request.getCode());
		}

		Users user = createBaseUser(request, Role.AGENT);
		String tempPassword = generatePasswordFromCode(request.getCode());
		user.setPassword(passwordEncoder.encode(tempPassword));
		Users savedUser = userRepository.save(user);

		AgentProfile profile = AgentProfile.builder()
				.users(savedUser)
				.agentCode(request.getCode().trim())
				.designation(request.getDesignation())
				.department(request.getDepartment())
				.assignedCity(request.getBranch())
				.assignedState(request.getBranchCode())
				.agentStatus(AgentStatus.ACTIVE)
				.build();
		agentProfileRepository.save(profile);

		String message = "AGENT created and credentials sent on email";
		try {
			mailService.sendStaffCredentials(savedUser.getEmail(), savedUser.getName(), "AGENT", request.getCode(), tempPassword);
		} catch (IllegalStateException ex) {
			message = "AGENT created, but email delivery failed. Share temp password manually.";
		}

		return buildResponse(savedUser, Role.AGENT, request.getCode(), tempPassword, message);
	}

	@Transactional
	public StaffCreateResponse createOfficer(CreateStaffRequest request) {
		validateCommonFields(request);
		if (request.getRole() != Role.OFFICER) {
			throw new IllegalArgumentException("Role must be OFFICER for this endpoint");
		}

		if (officerProfileRepository.existsByOfficerCode(request.getCode())) {
			throw new IllegalArgumentException("Officer code already exists: " + request.getCode());
		}

		Users user = createBaseUser(request, Role.OFFICER);
		String tempPassword = generatePasswordFromCode(request.getCode());
		user.setPassword(passwordEncoder.encode(tempPassword));
		Users savedUser = userRepository.save(user);

		OfficerProfile profile = OfficerProfile.builder()
				.users(savedUser)
				.officerCode(request.getCode().trim())
				.designation(request.getDesignation())
				.department(request.getDepartment())
				.branch(request.getBranch())
				.branchCode(request.getBranchCode())
				.officerStatus(OfficerStatus.ACTIVE)
				.build();
		officerProfileRepository.save(profile);

		String message = "OFFICER created and credentials sent on email";
		try {
			mailService.sendStaffCredentials(savedUser.getEmail(), savedUser.getName(), "OFFICER", request.getCode(), tempPassword);
		} catch (IllegalStateException ex) {
			message = "OFFICER created, but email delivery failed. Share temp password manually.";
		}

		return buildResponse(savedUser, Role.OFFICER, request.getCode(), tempPassword, message);
	}

	private Users createBaseUser(CreateStaffRequest request, Role role) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists: " + request.getEmail());
		}
		if (userRepository.existsByPhone(request.getPhone())) {
			throw new IllegalArgumentException("Phone already exists: " + request.getPhone());
		}

		return Users.builder()
				.name(request.getName().trim())
				.email(request.getEmail().trim().toLowerCase())
				.phone(request.getPhone().trim())
				.isHome(false)
				.address(request.getAddress())
				.role(role)
				.build();
	}

	private void validateCommonFields(CreateStaffRequest request) {
		if (request.getRole() == null) {
			throw new IllegalArgumentException("Role is required");
		}
		if (!StringUtils.hasText(request.getCode())) {
			throw new IllegalArgumentException("Code is required");
		}
		if (!StringUtils.hasText(request.getName())) {
			throw new IllegalArgumentException("Name is required");
		}
		if (!StringUtils.hasText(request.getEmail())) {
			throw new IllegalArgumentException("Email is required");
		}
	}

	private String generatePasswordFromCode(String code) {
		int suffix = 1000 + RANDOM.nextInt(9000);
		return code.trim() + "@" + suffix;
	}

	private StaffCreateResponse buildResponse(Users user, Role role, String code, String tempPassword, String message) {
		return StaffCreateResponse.builder()
				.userId(user.getId())
				.role(role)
				.code(code)
				.email(user.getEmail())
				.isHome(user.getIsHome() != null ? user.getIsHome() : false)
				.tempPassword(tempPassword)
				.message(message)
				.build();
	}
}

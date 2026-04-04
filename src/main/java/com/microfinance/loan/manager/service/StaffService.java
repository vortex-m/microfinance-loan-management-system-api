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
import com.microfinance.loan.manager.support.GeneratePass;
import com.microfinance.loan.manager.support.StaffRequestValidator;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private final StaffRequestValidator validator;
    private final GeneratePass generatePass;
    private final UserRepository userRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public StaffService(UserRepository userRepository,
                        AgentProfileRepository agentProfileRepository,
                        OfficerProfileRepository officerProfileRepository,
                        PasswordEncoder passwordEncoder,
                        MailService mailService,
                        StaffRequestValidator validator,
                        GeneratePass generatePass) {
        this.userRepository = userRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.validator = validator;
        this.generatePass = generatePass;
    }

    @Transactional
    public StaffCreateResponse createAgent(CreateStaffRequest request) {
        validator.validateCommonField(request);
        if (request.getRole() != Role.AGENT) {
            throw new IllegalArgumentException("Role must be AGENT for this endpoint");
        }

        String normalizedCode = request.getCode().trim().toUpperCase();
        if (agentProfileRepository.existsByAgentCode(normalizedCode)) {
            throw new IllegalArgumentException("Agent code already exists: " + normalizedCode);
        }

        Users user = createBaseUser(request, Role.AGENT);
        String tempPassword = generatePass.generatePassFromCode(normalizedCode);
        user.setPassword(passwordEncoder.encode(tempPassword));
        Users savedUser = userRepository.save(user);

        AgentProfile profile = AgentProfile.builder()
                .users(savedUser)
                .agentCode(normalizedCode)
                .designation(request.getDesignation().trim())
                .department(request.getDepartment().trim())
                .branch(request.getBranch().trim())
                .branchCode(request.getBranchCode().trim().toUpperCase())
                .agentStatus(AgentStatus.ACTIVE)
                .build();
        agentProfileRepository.save(profile);

        String message = "AGENT created and credentials sent on email";
        try {
            mailService.sendStaffCredentials(savedUser.getEmail(), savedUser.getName(), "AGENT", normalizedCode, tempPassword);
        } catch (IllegalStateException ex) {
            message = "AGENT created, but email delivery failed. Share temp password manually.";
        }

        return buildResponse(savedUser, Role.AGENT, normalizedCode, tempPassword, message);
    }

    @Transactional
    public StaffCreateResponse createOfficer(CreateStaffRequest request) {
        validator.validateCommonField(request);
        if (request.getRole() != Role.OFFICER) {
            throw new IllegalArgumentException("Role must be OFFICER for this endpoint");
        }

        String normalizedCode = request.getCode().trim().toUpperCase();
        if (officerProfileRepository.existsByOfficerCode(normalizedCode)) {
            throw new IllegalArgumentException("Officer code already exists: " + normalizedCode);
        }

        Users user = createBaseUser(request, Role.OFFICER);
        String tempPassword = generatePass.generatePassFromCode(normalizedCode);
        user.setPassword(passwordEncoder.encode(tempPassword));
        Users savedUser = userRepository.save(user);

        OfficerProfile profile = OfficerProfile.builder()
                .users(savedUser)
                .officerCode(normalizedCode)
                .designation(request.getDesignation().trim())
                .department(request.getDepartment().trim())
                .branch(request.getBranch().trim())
                .branchCode(request.getBranchCode().trim().toUpperCase())
                .officerStatus(OfficerStatus.ACTIVE)
                .build();
        officerProfileRepository.save(profile);

        String message = "OFFICER created and credentials sent on email";
        try {
            mailService.sendStaffCredentials(savedUser.getEmail(), savedUser.getName(), "OFFICER", normalizedCode, tempPassword);
        } catch (IllegalStateException ex) {
            message = "OFFICER created, but email delivery failed. Share temp password manually.";
        }

        return buildResponse(savedUser, Role.OFFICER, normalizedCode, tempPassword, message);
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

    private StaffCreateResponse buildResponse(Users user, Role role, String code, String tempPassword, String message) {
        return StaffCreateResponse.builder()
                .userId(user.getId())
                .role(role)
                .code(code)
                .email(user.getEmail())
                .isHome(Boolean.TRUE.equals(user.getIsHome()))
                .tempPassword(tempPassword)
                .message(message)
                .build();
    }
}


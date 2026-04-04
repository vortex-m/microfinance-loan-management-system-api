package com.microfinance.loan.common.service.impl;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;
    private final ManagerProfileRepository managerProfileRepository;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  AgentProfileRepository agentProfileRepository,
                                  OfficerProfileRepository officerProfileRepository,
                                  ManagerProfileRepository managerProfileRepository) {
        this.userRepository = userRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.officerProfileRepository = officerProfileRepository;
        this.managerProfileRepository = managerProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        Users users = resolveUser(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for login ID: " + loginId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(loginId)
                .password(users.getPassword())
                .roles(users.getRole().name())
                .build();
    }

    public Optional<Users> resolveUser(String loginId) {
        String normalized = loginId == null ? "" : loginId.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        Optional<Users> byEmail = userRepository.findByEmail(normalized.toLowerCase());
        if (byEmail.isPresent()) {
            return byEmail;
        }

        Optional<Users> byManagerCode = managerProfileRepository.findByManagerCodeWithUsers(normalized)
                .map(com.microfinance.loan.manager.entity.ManagerProfile::getUsers)
                .filter(user -> user.getRole() == Role.MANAGER);
        if (byManagerCode.isPresent()) {
            return byManagerCode;
        }

        Optional<Users> byAgentCode = agentProfileRepository.findByAgentCodeWithUsers(normalized)
                .map(com.microfinance.loan.agent.entity.AgentProfile::getUsers)
                .filter(user -> user.getRole() == Role.AGENT);
        if (byAgentCode.isPresent()) {
            return byAgentCode;
        }

        return officerProfileRepository.findByOfficerCodeWithUsers(normalized)
                .map(com.microfinance.loan.officer.entity.OfficerProfile::getUsers)
                .filter(user -> user.getRole() == Role.OFFICER);
    }
}
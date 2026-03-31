package com.microfinance.loan.common.service.impl;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

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

        Optional<Users> byManagerCode = managerProfileRepository.findByManagerCode(normalized)
                .map(profile -> profile.getUsers())
                .filter(user -> user.getRole() == Role.MANAGER);
        if (byManagerCode.isPresent()) {
            return byManagerCode;
        }

        Optional<Users> byAgentCode = agentProfileRepository.findByAgentCode(normalized)
                .map(profile -> profile.getUsers())
                .filter(user -> user.getRole() == Role.AGENT);
        if (byAgentCode.isPresent()) {
            return byAgentCode;
        }

        return officerProfileRepository.findByOfficerCode(normalized)
                .map(profile -> profile.getUsers())
                .filter(user -> user.getRole() == Role.OFFICER);
    }
}
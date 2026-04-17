package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.response.AgentProfileResponse;
import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProfileServiceTest {

    @Mock
    private AgentProfileRepository agentProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AgentProfileService agentProfileService;

    @Test
    void updateAvailability_shouldPersistAvailability() {
        Users user = Users.builder().id(88L).name("Agent One").email("agent@example.com").phone("9999999999").build();
        AgentProfile profile = AgentProfile.builder().id(12L).agentCode("AG001").users(user).agentAvailability(AgentAvailability.AVAILABLE).build();

        when(userRepository.findById(88L)).thenReturn(Optional.of(user));
        when(agentProfileRepository.findByUsersId(88L)).thenReturn(Optional.of(profile));
        when(agentProfileRepository.save(any(AgentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentProfileResponse response = agentProfileService.updateAvailability(88L, AgentAvailability.BUSY);

        assertEquals(AgentAvailability.BUSY, response.getAgentAvailability());
    }
}


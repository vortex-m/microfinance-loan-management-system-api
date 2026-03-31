package com.microfinance.loan.manager.service;

import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.MailService;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgentProfileRepository agentProfileRepository;

    @Mock
    private OfficerProfileRepository officerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ManagerService managerService;

    @Test
    void createOfficer_shouldCreateOfficerAndSendEmail() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.OFFICER)
                .name("Ravi Officer")
                .email("ravi.officer@example.com")
                .phone("9876543211")
                .code("OFF001")
                .designation("Loan Officer")
                .department("Credit")
                .branch("Bhopal")
                .branchCode("BPL01")
                .address("Bhopal")
                .build();

        when(officerProfileRepository.existsByOfficerCode("OFF001")).thenReturn(false);
        when(userRepository.existsByEmail("ravi.officer@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543211")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setId(21L);
            return user;
        });
        doNothing().when(mailService).sendStaffCredentials(anyString(), anyString(), anyString(), anyString(), anyString());

        StaffCreateResponse response = managerService.createOfficer(request);

        assertEquals(21L, response.getUserId());
        assertEquals(Role.OFFICER, response.getRole());
        assertEquals("OFF001", response.getCode());
        assertTrue(response.getTempPassword().startsWith("OFF001@"));
        assertEquals("OFFICER created and credentials sent on email", response.getMessage());

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
        assertEquals(Role.OFFICER, userCaptor.getValue().getRole());
    }

    @Test
    void createOfficer_shouldReturnWarningWhenEmailFails() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.OFFICER)
                .name("Ravi Officer")
                .email("ravi.officer@example.com")
                .phone("9876543211")
                .code("OFF001")
                .build();

        when(officerProfileRepository.existsByOfficerCode("OFF001")).thenReturn(false);
        when(userRepository.existsByEmail("ravi.officer@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543211")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setId(33L);
            return user;
        });

        doThrow(new IllegalStateException("smtp failed"))
                .when(mailService)
                .sendStaffCredentials(anyString(), anyString(), anyString(), anyString(), anyString());

        StaffCreateResponse response = managerService.createOfficer(request);

        assertEquals(33L, response.getUserId());
        assertEquals("OFFICER created, but email delivery failed. Share temp password manually.", response.getMessage());
        assertTrue(response.getTempPassword().startsWith("OFF001@"));
    }
}



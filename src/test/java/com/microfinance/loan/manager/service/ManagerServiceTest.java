package com.microfinance.loan.manager.service;

import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private StaffService staffService;

    @InjectMocks
    private ManagerService managerService;

    @Test
    void createOfficer_shouldDelegateToStaffService() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.OFFICER)
                .name("Ravi Kumar")
                .email("mayank657585@gmail.com")
                .phone("9876543211")
                .code("OFF001")
                .build();

        StaffCreateResponse expected = StaffCreateResponse.builder()
                .userId(21L)
                .role(Role.OFFICER)
                .code("OFF001")
                .message("OFFICER created and credentials sent on email")
                .build();

        when(staffService.createOfficer(request)).thenReturn(expected);

        StaffCreateResponse actual = managerService.createOfficer(request);

        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        verify(staffService).createOfficer(request);
    }

    @Test
    void createAgent_shouldDelegateToStaffService() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.AGENT)
                .name("Rohan Kumar")
                .email("mayank6343@gmail.com")
                .phone("9876543222")
                .code("AG001")
                .build();

        StaffCreateResponse expected = StaffCreateResponse.builder()
                .userId(33L)
                .role(Role.AGENT)
                .code("AG001")
                .message("AGENT created and credentials sent on email")
                .build();

        when(staffService.createAgent(request)).thenReturn(expected);

        StaffCreateResponse actual = managerService.createAgent(request);

        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        verify(staffService).createAgent(request);
    }
}

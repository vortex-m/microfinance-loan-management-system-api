package com.microfinance.loan.manager.service;

import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.common.enums.ManagerDepartment;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.service.CurrentUserService;
import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import com.microfinance.loan.manager.entity.ManagerProfile;
import com.microfinance.loan.manager.repository.ManagerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private StaffService staffService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ManagerProfileRepository managerProfileRepository;


    @Mock
    private Authentication authentication;

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
                .department(ManagerDepartment.BRANCH_OPERATIONS)
                .branchCode("BR001")
                .branch("Main Branch")
                .build();

        BranchProfile branchProfile = BranchProfile.builder()
                .branchCode("BR001")
                .branchName("Main Branch")
                .active(true)
                .build();

        ManagerProfile managerProfile = ManagerProfile.builder()
                .department(ManagerDepartment.BRANCH_OPERATIONS.name())
                .branchProfile(branchProfile)
                .build();

        StaffCreateResponse expected = StaffCreateResponse.builder()
                .userId(21L)
                .role(Role.OFFICER)
                .code("OFF001")
                .message("OFFICER created and credentials sent on email")
                .build();

        when(currentUserService.getCurrentUserId(authentication)).thenReturn(21L);
        when(managerProfileRepository.findByUsersIdWithBranch(21L)).thenReturn(Optional.of(managerProfile));
        when(staffService.createOfficer(managerProfile, request)).thenReturn(expected);

        StaffCreateResponse actual = managerService.createOfficer(authentication, request);

        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        verify(staffService).createOfficer(managerProfile, request);
    }

    @Test
    void createAgent_shouldDelegateToStaffService() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.AGENT)
                .name("Rohan Kumar")
                .email("mayank6343@gmail.com")
                .phone("9876543222")
                .code("AG001")
                .department(ManagerDepartment.BRANCH_OPERATIONS)
                .branchCode("BR001")
                .branch("Main Branch")
                .build();

        BranchProfile branchProfile = BranchProfile.builder()
                .branchCode("BR001")
                .branchName("Main Branch")
                .active(true)
                .build();

        ManagerProfile managerProfile = ManagerProfile.builder()
                .department(ManagerDepartment.BRANCH_OPERATIONS.name())
                .branchProfile(branchProfile)
                .build();

        StaffCreateResponse expected = StaffCreateResponse.builder()
                .userId(33L)
                .role(Role.AGENT)
                .code("AG001")
                .message("AGENT created and credentials sent on email")
                .build();

        when(currentUserService.getCurrentUserId(authentication)).thenReturn(33L);
        when(managerProfileRepository.findByUsersIdWithBranch(33L)).thenReturn(Optional.of(managerProfile));
        when(staffService.createAgent(managerProfile, request)).thenReturn(expected);

        StaffCreateResponse actual = managerService.createAgent(authentication, request);

        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        verify(staffService).createAgent(managerProfile, request);
    }

    @Test
    void createManager_shouldDelegateToStaffService() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .role(Role.MANAGER)
                .name("Amit Kumar")
                .email("amit.manager@gmail.com")
                .phone("9876543255")
                .code("MGR101")
                .designation("Credit Manager")
                .department(ManagerDepartment.CREDIT_UNDERWRITING)
                .branchCode("BR001")
                .branch("Main Branch")
                .build();

        BranchProfile branchProfile = BranchProfile.builder()
                .branchCode("BR001")
                .branchName("Main Branch")
                .active(true)
                .build();

        ManagerProfile managerProfile = ManagerProfile.builder()
                .department(ManagerDepartment.BRANCH_OPERATIONS.name())
                .branchProfile(branchProfile)
                .build();

        StaffCreateResponse expected = StaffCreateResponse.builder()
                .userId(41L)
                .role(Role.MANAGER)
                .code("MGR101")
                .message("MANAGER created and credentials sent on email")
                .build();

        when(currentUserService.getCurrentUserId(authentication)).thenReturn(41L);
        when(managerProfileRepository.findByUsersIdWithBranch(41L)).thenReturn(Optional.of(managerProfile));
        when(staffService.createManager(managerProfile, request)).thenReturn(expected);

        StaffCreateResponse actual = managerService.createManager(authentication, request);

        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        verify(staffService).createManager(managerProfile, request);
    }
}

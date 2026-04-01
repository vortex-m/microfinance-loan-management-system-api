package com.microfinance.loan.manager.service;

import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import com.microfinance.loan.manager.dto.response.StaffCreateResponse;
import org.springframework.stereotype.Service;


@Service
public class ManagerService {

    private final StaffService staffService;

    public ManagerService(StaffService staffService) {
        this.staffService = staffService;
    }

    public StaffCreateResponse createAgent(CreateStaffRequest request) {
        return staffService.createAgent(request);
    }

    public StaffCreateResponse createOfficer(CreateStaffRequest request) {
        return staffService.createOfficer(request);
    }
}

package com.microfinance.loan.manager.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.manager.dto.response.ManagerUserResponse;
import com.microfinance.loan.manager.service.ManagerUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/users")
public class ManagerUserController {

    private final ManagerUserService managerUserService;
    public ManagerUserController(ManagerUserService managerUserService) {
        this.managerUserService = managerUserService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/getAll")
    public ApiResponse<List<ManagerUserResponse>> getAll(){
        return ApiResponse.success("Users fetched successfully", managerUserService.getAllUser());
    }
}


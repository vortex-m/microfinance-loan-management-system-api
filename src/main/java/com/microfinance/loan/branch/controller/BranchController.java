package com.microfinance.loan.branch.controller;

import com.microfinance.loan.branch.dto.request.BranchUpsertRequest;
import com.microfinance.loan.branch.dto.response.BranchResponse;
import com.microfinance.loan.branch.service.BranchService;
import com.microfinance.loan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/upsert")
    public ApiResponse<BranchResponse> upsert(@Valid @RequestBody BranchUpsertRequest request) {
        return ApiResponse.success("Branch saved successfully", branchService.upsert(request));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ApiResponse<List<BranchResponse>> getAll() {
        return ApiResponse.success("Branches fetched successfully", branchService.getAll());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{branchCode}")
    public ApiResponse<BranchResponse> getByCode(@PathVariable String branchCode) {
        return ApiResponse.success("Branch fetched successfully", branchService.getByCode(branchCode));
    }
}


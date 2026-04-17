package com.microfinance.loan.officer.controller;

import com.microfinance.loan.common.dto.ApiResponse;
import com.microfinance.loan.officer.dto.request.CashSettlementRequest;
import com.microfinance.loan.officer.dto.response.CashSettlementResponse;
import com.microfinance.loan.officer.service.OfficerCashSettlementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/officers/cash-settlements")
public class OfficerCashSettlementController {

    private final OfficerCashSettlementService officerCashSettlementService;

    public OfficerCashSettlementController(OfficerCashSettlementService officerCashSettlementService) {
        this.officerCashSettlementService = officerCashSettlementService;
    }

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping
    public ApiResponse<CashSettlementResponse> settleCash(Authentication authentication,
                                                          @Valid @RequestBody CashSettlementRequest request) {
        return ApiResponse.success(
                "Cash settlements recorded successfully",
                officerCashSettlementService.settleCash(authentication, request)
        );
    }
}


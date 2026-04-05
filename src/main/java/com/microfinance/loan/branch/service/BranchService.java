package com.microfinance.loan.branch.service;

import com.microfinance.loan.branch.dto.request.BranchUpsertRequest;
import com.microfinance.loan.branch.dto.response.BranchResponse;
import com.microfinance.loan.branch.entity.BranchProfile;
import com.microfinance.loan.branch.repository.BranchProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class BranchService {

    private final BranchProfileRepository branchProfileRepository;

    public BranchService(BranchProfileRepository branchProfileRepository) {
        this.branchProfileRepository = branchProfileRepository;
    }

    @Transactional
    public BranchResponse upsert(BranchUpsertRequest request) {
        String branchCode = normalizeCode(request.getBranchCode());

        BranchProfile profile = branchProfileRepository.findByBranchCodeIgnoreCase(branchCode)
                .orElseGet(BranchProfile::new);

        profile.setBranchCode(branchCode);
        profile.setBranchName(request.getBranchName().trim());
        profile.setRegionCode(normalizeCode(request.getRegionCode()));
        profile.setRegionName(request.getRegionName().trim());
        profile.setCity(trimOrNull(request.getCity()));
        profile.setState(trimOrNull(request.getState()));
        profile.setAddress(trimOrNull(request.getAddress()));
        profile.setPincode(trimOrNull(request.getPincode()));
        if (request.getActive() != null) {
            profile.setActive(request.getActive());
        }

        return toResponse(branchProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAll() {
        return branchProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse getByCode(String branchCode) {
        BranchProfile profile = branchProfileRepository.findByBranchCodeIgnoreCase(branchCode)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchCode));
        return toResponse(profile);
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }

    private BranchResponse toResponse(BranchProfile profile) {
        return BranchResponse.builder()
                .id(profile.getId())
                .branchCode(profile.getBranchCode())
                .branchName(profile.getBranchName())
                .regionCode(profile.getRegionCode())
                .regionName(profile.getRegionName())
                .city(profile.getCity())
                .state(profile.getState())
                .address(profile.getAddress())
                .pincode(profile.getPincode())
                .active(profile.getActive())
                .build();
    }
}


package com.microfinance.loan.manager.support;


import com.microfinance.loan.manager.dto.request.CreateStaffRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StaffRequestValidator {
    public void validateCommonField(CreateStaffRequest request) {
        if(request.getRole() == null){
            throw new IllegalArgumentException("Role is required.");
        }
        if(!StringUtils.hasText(request.getCode())){
            throw new IllegalArgumentException("Code is required.");
        }
        if(!StringUtils.hasText((request.getBranch()))){
            throw new IllegalArgumentException("Branch is required.");
        }
        if(!StringUtils.hasText(request.getName())){
            throw new IllegalArgumentException("Name is required.");
        }
        if(!StringUtils.hasText(request.getEmail())){
            throw new IllegalArgumentException("Email is required.");
        }
        if(!StringUtils.hasText(request.getPhone())){
            throw new IllegalArgumentException("Phone is required.");
        }
    }
}

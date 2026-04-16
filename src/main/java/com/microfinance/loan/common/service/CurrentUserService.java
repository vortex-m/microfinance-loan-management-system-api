package com.microfinance.loan.common.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserDetailsServiceImpl userDetailsService;

    public CurrentUserService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public Long getCurrentUserId(Authentication auth){
        if(auth == null || auth.getName() == null){
            throw new AccessDeniedException("Unauthenticated user.");
        }
        return userDetailsService.resolveUser(auth.getName())
                .map(u -> u.getId())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found."));
    }

    public Users getCurrentUser(Authentication auth) {
        if(auth == null || auth.getName() == null){
            throw new AccessDeniedException("Unauthenticated user.");
        }
        return userDetailsService.resolveUser(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found."));
    }
}

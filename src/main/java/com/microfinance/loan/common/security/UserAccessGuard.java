package com.microfinance.loan.common.security;

import com.microfinance.loan.common.service.impl.UserDetailsServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccessGuard")
public class UserAccessGuard {
    private final UserDetailsServiceImpl userDetailsService;

    public UserAccessGuard(UserDetailsServiceImpl userDetailsService){
        this.userDetailsService = userDetailsService;
    }

    public boolean canAccessUser(Long userId, Authentication auth){
        if(auth == null || userId == null){
            return false;
        }
        return userDetailsService.resolveUser(auth.getName())
                .map(u -> userId.equals((u.getId())))
                .orElse(false);
    }
}

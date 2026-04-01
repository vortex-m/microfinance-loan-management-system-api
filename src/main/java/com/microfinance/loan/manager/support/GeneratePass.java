package com.microfinance.loan.manager.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneratePass {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generatePassFromCode(String code){
        int suffix = 1000 + RANDOM.nextInt(9000);
        return code.trim() + "@" + suffix;
    }
}

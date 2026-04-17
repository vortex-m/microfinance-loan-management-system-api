package com.microfinance.loan.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AiScoreRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(AiScoreRefreshScheduler.class);

    private final AiService aiService;

    @Value("${ai.scoring.enabled:true}")
    private boolean enabled;

    public AiScoreRefreshScheduler(AiService aiService) {
        this.aiService = aiService;
    }

    @Scheduled(cron = "${ai.scoring.cron:0 30 0 * * *}")
    public void refreshScoresDaily() {
        if (!enabled) {
            return;
        }
        int refreshed = aiService.refreshScoresForAllUsers(true);
        log.info("AI score refresh completed. Updated users: {}", refreshed);
    }
}


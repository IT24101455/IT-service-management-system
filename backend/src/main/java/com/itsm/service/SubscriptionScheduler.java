package com.itsm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SubscriptionScheduler {

    @Autowired
    private TechnicianSubscriptionService subscriptionService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        // Rule: Deactivate on the 5th of every month if not paid
        if (now.getDayOfMonth() >= 5) {
            subscriptionService.deactivateUnpaidTechnicians();
        }
    }
}

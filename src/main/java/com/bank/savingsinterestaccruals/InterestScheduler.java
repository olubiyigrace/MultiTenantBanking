package com.bank.savingsinterestaccruals;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class InterestScheduler {
    private final InterestAccrualService interestAccrualService;


    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Lagos")
    public void processDailyInterestAccrual() {
        log.info("Starting daily interest accrual process");

        try {
            interestAccrualService.processDailyInterest();
            log.info("Daily interest accrual completed successfully");
        } catch (Exception ex) {
            log.error("Daily interest accrual failed", ex);
        }
    }


}

package com.wrkr.tickety.domains.member.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailResendScheduler {

    private final EmailResendService emailResendService;

    // 5분마다 재전송 시도
    @Scheduled(fixedDelay = 300_000)
    public void retrySendEmails() {
        emailResendService.resendEmails();
    }
}

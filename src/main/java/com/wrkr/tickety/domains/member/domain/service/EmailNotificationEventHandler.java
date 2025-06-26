package com.wrkr.tickety.domains.member.domain.service;

import com.wrkr.tickety.domains.member.domain.model.EmailBulkNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationEventHandler {

    private final EmailNotificationService emailNotificationService;

    @Async
    @EventListener
    public void handleEmailNotificationCreated(EmailBulkNotificationEvent event) {
        emailNotificationService.processEmailNotifications(event.getEmails());
    }
}
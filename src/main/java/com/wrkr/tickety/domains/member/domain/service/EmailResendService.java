package com.wrkr.tickety.domains.member.domain.service;

import com.wrkr.tickety.domains.member.application.mapper.EmailMapper;
import com.wrkr.tickety.domains.member.domain.constant.EmailNotiStatus;
import com.wrkr.tickety.domains.member.persistence.entity.EmailNotificationEntity;
import com.wrkr.tickety.domains.member.persistence.repository.EmailNotificationRepository;
import com.wrkr.tickety.infrastructure.email.EmailConstants;
import com.wrkr.tickety.infrastructure.email.EmailCreateRequest;
import com.wrkr.tickety.infrastructure.email.EmailUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailResendService {

    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailUtil emailUtil;

    @Async
    public void resendEmails() {
        List<EmailNotificationEntity> emailNotifications =
            emailNotificationRepository.findTop100ByStatusInAndRetryCountLessThan(List.of(EmailNotiStatus.PENDING, EmailNotiStatus.FAILED), 5);

        List<String> successEmails = new ArrayList<>();
        List<String> failedEmails = new ArrayList<>();


        for (EmailNotificationEntity emailNotification : emailNotifications) {
            try {
                EmailCreateRequest request = EmailMapper.toEmailCreateRequest(
                    emailNotification.getEmail(),
                    EmailConstants.TEMP_PASSWORD_SUBJECT,
                    null
                );
                emailUtil.sendMail(request, emailNotification.getTempPassword(), EmailConstants.FILENAME_PASSWORD);
                successEmails.add(emailNotification.getEmail());
            } catch (Exception e) {
                log.error("[EmailNotificationService] 이메일 전송 실패: email={}, error={}", emailNotification.getEmail(), e.getMessage());
                failedEmails.add(emailNotification.getEmail());
            }
        }

        if (!successEmails.isEmpty()) {
            emailNotificationRepository.bulkUpdateStatusByEmails(EmailNotiStatus.SENT, successEmails);
        }
        if (!failedEmails.isEmpty()) {
            emailNotificationRepository.bulkUpdateStatusByEmails(EmailNotiStatus.FAILED, failedEmails);
        }
    }
}
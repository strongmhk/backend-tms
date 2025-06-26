package com.wrkr.tickety.domains.member.domain.service;

import com.wrkr.tickety.domains.member.application.mapper.EmailMapper;
import com.wrkr.tickety.domains.member.domain.constant.EmailNotiStatus;
import com.wrkr.tickety.domains.member.persistence.entity.EmailNotificationEntity;
import com.wrkr.tickety.domains.member.persistence.repository.EmailNotificationRepository;
import com.wrkr.tickety.infrastructure.email.EmailConstants;
import com.wrkr.tickety.infrastructure.email.EmailCreateRequest;
import com.wrkr.tickety.infrastructure.email.EmailUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailUtil emailUtil;

    @Transactional
    public void bulkInsertEmailNotifications(List<EmailNotificationEntity> notifications) {
        String sql = """
        INSERT INTO email_notification (
            email, temp_password, status, retry_count, created_at, updated_at
        ) VALUES (?, ?, ?, ?, NOW(), NOW())
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EmailNotificationEntity e = notifications.get(i);
                ps.setString(1, e.getEmail());
                ps.setString(2, e.getTempPassword());
                ps.setString(3, e.getStatus().name());
                ps.setInt(4, e.getRetryCount());
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
    }

    @Transactional
    public void processEmailNotifications(List<String> emails) {
        List<EmailNotificationEntity> emailNotifications = emailNotificationRepository.findByEmailIn(emails);

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

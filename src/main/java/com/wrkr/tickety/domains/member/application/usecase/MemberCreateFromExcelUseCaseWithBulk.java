package com.wrkr.tickety.domains.member.application.usecase;

import com.wrkr.tickety.domains.auth.utils.PasswordEncoderUtil;
import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.domain.constant.EmailNotiStatus;
import com.wrkr.tickety.domains.member.domain.model.EmailBulkNotificationEvent;
import com.wrkr.tickety.domains.member.domain.service.EmailNotificationService;
import com.wrkr.tickety.domains.member.domain.service.MemberSaveService;
import com.wrkr.tickety.domains.member.persistence.entity.EmailNotificationEntity;
import com.wrkr.tickety.domains.member.presentation.util.validator.MemberFieldValidator;
import com.wrkr.tickety.global.annotation.architecture.UseCase;
import com.wrkr.tickety.global.utils.RandomCodeGenerator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class MemberCreateFromExcelUseCaseWithBulk {

    private final MemberSaveService memberSaveService;
    private final MemberFieldValidator memberFieldValidator;
    private final EmailNotificationService emailNotificationService;
    private final ApplicationEventPublisher eventPublisher;

    public void createMembersWithEmailNotification(List<MemberCreateRequestForExcel> requests) {
        // 1. 비밀번호 생성
        List<String> tempPasswords = requests.stream()
            .map(req -> RandomCodeGenerator.generateUUID().substring(0, 12))
            .toList();
        List<String> encryptedPasswords = tempPasswords.stream()
            .map(PasswordEncoderUtil::encodePassword)
            .toList();

        // 2. 회원 벌크 저장
        memberSaveService.bulkInsertMember(requests, encryptedPasswords);

        // 3. EmailNotification 벌크 저장
        List<EmailNotificationEntity> notifications = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            MemberCreateRequestForExcel request = requests.get(i);
            String tempPassword = tempPasswords.get(i);
            notifications.add(
                EmailNotificationEntity.builder()
                    .email(request.getEmail())
                    .tempPassword(tempPassword) // TODO: 비밀번호 암호화 고려
                    .status(EmailNotiStatus.PENDING)
                    .retryCount(0)
                    .build()
            );
        }
        emailNotificationService.bulkInsertEmailNotifications(notifications);

        // 4. 저장된 이메일로 Event 발행
        List<String> savedEmails = requests.stream()
            .map(MemberCreateRequestForExcel::getEmail)
            .toList();

        eventPublisher.publishEvent(new EmailBulkNotificationEvent(savedEmails));
    }
}

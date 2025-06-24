package com.wrkr.tickety.domains.member.application.usecase;

import com.wrkr.tickety.domains.auth.utils.PasswordEncoderUtil;
import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.application.mapper.EmailMapper;
import com.wrkr.tickety.domains.member.domain.service.MemberSaveService;
import com.wrkr.tickety.domains.member.presentation.util.validator.MemberFieldValidator;
import com.wrkr.tickety.global.annotation.architecture.UseCase;
import com.wrkr.tickety.global.utils.RandomCodeGenerator;
import com.wrkr.tickety.infrastructure.email.EmailConstants;
import com.wrkr.tickety.infrastructure.email.EmailCreateRequest;
import com.wrkr.tickety.infrastructure.email.EmailUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class MemberCreateFromExcelUseCaseWithBulk {

    private final MemberSaveService memberSaveService;
    private final EmailUtil emailUtil;
    private final MemberFieldValidator memberFieldValidator;

    public void createMember(List<MemberCreateRequestForExcel> memberCreateRequestForExcels) {
        List<String> tempPasswords = new ArrayList<>();
        List<String> encryptedPasswords = new ArrayList<>();
        List<EmailCreateRequest> emailRequests = new ArrayList<>();

        for (MemberCreateRequestForExcel request : memberCreateRequestForExcels) {
            // 유효성 검증
            memberFieldValidator.validateField(
                request.getName(),
                request.getDepartment(),
                request.getPosition(),
                request.getPhone(),
                request.getRole(),
                request.getNickname(),
                request.getEmail()
            );

            // 임시 비밀번호 생성
            String tempPassword = RandomCodeGenerator.generateUUID().substring(0, 12);
            String encryptedPassword = PasswordEncoderUtil.encodePassword(tempPassword);
            tempPasswords.add(tempPassword);
            encryptedPasswords.add(encryptedPassword);

            // 이메일 요청
            EmailCreateRequest emailCreateRequest = EmailMapper.toEmailCreateRequest(
                request.getEmail(),
                EmailConstants.TEMP_PASSWORD_SUBJECT,
                null
            );
            emailRequests.add(emailCreateRequest);
        }

        // 대량 저장 처리
        memberSaveService.bulkInsertMember(memberCreateRequestForExcels, encryptedPasswords);

        // 이메일 전송
        sendEmails(emailRequests, tempPasswords);

    }

    private void sendEmails(List<EmailCreateRequest> emailRequests, List<String> tempPasswords) {
        for (int i = 0; i < emailRequests.size(); i++) {
            emailUtil.sendMail(emailRequests.get(i), tempPasswords.get(i), EmailConstants.FILENAME_PASSWORD);
        }
    }
}

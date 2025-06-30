package com.wrkr.tickety.domains.member.application.usecase;

import com.wrkr.tickety.domains.auth.utils.PasswordEncoderUtil;
import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.application.dto.response.MemberPkResponse;
import com.wrkr.tickety.domains.member.application.mapper.EmailMapper;
import com.wrkr.tickety.domains.member.application.mapper.MemberMapper;
import com.wrkr.tickety.domains.member.domain.service.MemberGetService;
import com.wrkr.tickety.domains.member.domain.service.MemberSaveService;
import com.wrkr.tickety.domains.member.presentation.util.validator.MemberFieldValidator;
import com.wrkr.tickety.global.annotation.architecture.UseCase;
import com.wrkr.tickety.global.utils.PkCrypto;
import com.wrkr.tickety.global.utils.RandomCodeGenerator;
import com.wrkr.tickety.infrastructure.email.EmailConstants;
import com.wrkr.tickety.infrastructure.email.EmailCreateRequest;
import com.wrkr.tickety.infrastructure.email.EmailUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Member Insert 벌크 연산 반영
@UseCase
@RequiredArgsConstructor
@Slf4j
public class MemberCreateFromExcelUseCaseV2 {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://i.ibb.co/7Fd4Hhx/tickety-default-image.jpg";

    private final MemberSaveService memberSaveService;
    private final EmailUtil emailUtil;
    private final MemberFieldValidator memberFieldValidator;
    private final MemberGetService memberGetService;

    public List<MemberPkResponse> createMember(List<MemberCreateRequestForExcel> requests) {
        // 전체 데이터를 한 번에 검증
        memberFieldValidator.validateBulk(requests);

        List<String> tempPasswords = new ArrayList<>();
        List<String> encryptedPasswords = new ArrayList<>();

        // 비밀번호 생성 및 암호화
        for (MemberCreateRequestForExcel request : requests) {
            String tempPassword = RandomCodeGenerator.generateUUID().substring(0, 12);
            tempPasswords.add(tempPassword);
            encryptedPasswords.add(PasswordEncoderUtil.encodePassword(tempPassword));
        }

        // Bulk 저장
        memberSaveService.bulkInsertMember(requests, encryptedPasswords);

        // 이메일 발송
        for (int i = 0; i < requests.size(); i++) {
            EmailCreateRequest emailRequest = EmailMapper.toEmailCreateRequest(
                requests.get(i).getEmail(),
                EmailConstants.TEMP_PASSWORD_SUBJECT,
                null
            );
            emailUtil.sendMail(emailRequest, tempPasswords.get(i), EmailConstants.FILENAME_PASSWORD);
        }

        // 저장된 Member 엔티티 조회 후 PK 반환
        return requests.stream()
            .map(request -> {
                Long memberId = memberGetService.findMemberIdByEmail(request.getEmail());
                return MemberMapper.toMemberPkResponse(PkCrypto.encrypt(memberId));
            })
            .toList();
    }
}

package com.wrkr.tickety.domains.member.application.usecase;

import com.wrkr.tickety.domains.auth.utils.PasswordEncoderUtil;
import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.application.dto.response.MemberPkResponse;
import com.wrkr.tickety.domains.member.application.mapper.EmailMapper;
import com.wrkr.tickety.domains.member.application.mapper.MemberMapper;
import com.wrkr.tickety.domains.member.domain.model.Member;
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
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCreateFromExcelUseCaseV1 {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://i.ibb.co/7Fd4Hhx/tickety-default-image.jpg";

    private final MemberSaveService memberSaveService;
    private final EmailUtil emailUtil;
    private final MemberFieldValidator memberFieldValidator;

    public List<MemberPkResponse> createMember(List<MemberCreateRequestForExcel> memberCreateRequestForExcels) {
        List<MemberPkResponse> memberPkResponses = new ArrayList<>();
        List<EmailCreateRequest> emailRequests = new ArrayList<>();
        List<String> tempPasswords = new ArrayList<>();

        for (MemberCreateRequestForExcel memberCreateRequestForExcel : memberCreateRequestForExcels) {
            memberFieldValidator.validateField(
                memberCreateRequestForExcel.getName(),
                memberCreateRequestForExcel.getDepartment(),
                memberCreateRequestForExcel.getPosition(),
                memberCreateRequestForExcel.getPhone(),
                memberCreateRequestForExcel.getRole(),
                memberCreateRequestForExcel.getNickname(),
                memberCreateRequestForExcel.getEmail()
            );

            String tempPassword = RandomCodeGenerator.generateUUID().substring(0, 12);
            String encryptedPassword = PasswordEncoderUtil.encodePassword(tempPassword);

            String profileImageUrl =
                memberCreateRequestForExcel.getProfileImage() == null ? DEFAULT_PROFILE_IMAGE_URL : memberCreateRequestForExcel.getProfileImage();

            String agitUrl = memberCreateRequestForExcel.getAgitUrl();
            Member createdMember = memberSaveService.save(
                MemberMapper.mapToMemberFromExcel(memberCreateRequestForExcel, encryptedPassword, profileImageUrl, agitUrl));

            EmailCreateRequest emailCreateRequest = EmailMapper.toEmailCreateRequest(
                createdMember.getEmail(),
                EmailConstants.TEMP_PASSWORD_SUBJECT,
                null
            );

            emailRequests.add(emailCreateRequest);
            tempPasswords.add(tempPassword);

            MemberPkResponse memberPkResponse = MemberMapper.toMemberPkResponse(PkCrypto.encrypt(createdMember.getMemberId()));

            memberPkResponses.add(memberPkResponse);
        }

        sendEmails(emailRequests, tempPasswords);

        return memberPkResponses;
    }

    private void sendEmails(List<EmailCreateRequest> emailRequests, List<String> tempPasswords) {
        for (int i = 0; i < emailRequests.size(); i++) {
            emailUtil.sendMail(emailRequests.get(i), tempPasswords.get(i), EmailConstants.FILENAME_PASSWORD);
        }
    }
}

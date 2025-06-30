package com.wrkr.tickety.domains.member.presentation;

import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.ALREADY_EXIST_EMAIL;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.ALREADY_EXIST_NICKNAME;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_DEPARTMENT;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_EMAIL;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_NAME;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_NICKNAME;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_PHONE;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_POSITION;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.INVALID_ROLE;
import static com.wrkr.tickety.domains.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.EXCEED_MAX_FILE_SIZE;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.FILE_NOT_UPLOAD;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.INVALID_EXCEL_EXTENSION;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.INVALID_IMAGE_EXTENSION;
import static com.wrkr.tickety.global.response.code.CommonErrorCode.METHOD_ARGUMENT_NOT_VALID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequest;
import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.application.dto.request.MemberDeleteRequest;
import com.wrkr.tickety.domains.member.application.dto.request.MemberInfoUpdateRequest;
import com.wrkr.tickety.domains.member.application.dto.response.MemberInfoPreviewResponse;
import com.wrkr.tickety.domains.member.application.dto.response.MemberInfoResponse;
import com.wrkr.tickety.domains.member.application.dto.response.MemberPkResponse;
import com.wrkr.tickety.domains.member.application.usecase.ExcelExampleCreateUseCase;
import com.wrkr.tickety.domains.member.application.usecase.MemberCreateFromExcelUseCaseV1;
import com.wrkr.tickety.domains.member.application.usecase.MemberCreateFromExcelUseCaseV2;
import com.wrkr.tickety.domains.member.application.usecase.MemberCreateFromExcelUseCaseV3;
import com.wrkr.tickety.domains.member.application.usecase.MemberCreateUseCase;
import com.wrkr.tickety.domains.member.application.usecase.MemberInfoGetUseCase;
import com.wrkr.tickety.domains.member.application.usecase.MemberInfoSearchUseCase;
import com.wrkr.tickety.domains.member.application.usecase.MemberInfoUpdateUseCase;
import com.wrkr.tickety.domains.member.domain.constant.Role;
import com.wrkr.tickety.domains.member.domain.model.Member;
import com.wrkr.tickety.global.annotation.swagger.CustomErrorCodes;
import com.wrkr.tickety.global.common.dto.ApplicationPageRequest;
import com.wrkr.tickety.global.common.dto.ApplicationPageResponse;
import com.wrkr.tickety.global.response.ApplicationResponse;
import com.wrkr.tickety.global.utils.excel.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
@Tag(name = "Admin Member Controller")
@Validated
public class AdminMemberController {

    private final MemberCreateFromExcelUseCaseV3 memberCreateFromExcelUseCaseV3;
    private final MemberCreateUseCase memberCreateUseCase;
    private final MemberInfoUpdateUseCase memberInfoUpdateUseCase;
    private final MemberInfoGetUseCase memberInfoGetUseCase;
    private final MemberInfoSearchUseCase memberInfoSearchUseCase;
    private final ExcelExampleCreateUseCase excelExampleCreateUseCase;
    private final ExcelUtil excelUtil;

    @CustomErrorCodes(
        memberErrorCodes = {
            INVALID_EMAIL, ALREADY_EXIST_EMAIL,
            INVALID_NAME,
            INVALID_NICKNAME, ALREADY_EXIST_NICKNAME,
            INVALID_DEPARTMENT,
            INVALID_POSITION,
            INVALID_PHONE,
            INVALID_ROLE
        },
        commonErrorCodes = {
            INVALID_IMAGE_EXTENSION,
            EXCEED_MAX_FILE_SIZE
        }
    )
    @Operation(summary = "관리자 - 회원 등록", description = "회원을 등록 시 이메일로 임시 비밀번호를 발급합니다.")
    @PostMapping(consumes = {MULTIPART_FORM_DATA_VALUE, APPLICATION_JSON_VALUE})
    public ApplicationResponse<MemberPkResponse> createMember(
        @AuthenticationPrincipal Member member,
        @RequestPart(value = "request") @Valid MemberCreateRequest memberCreateRequest,
        @RequestPart(required = false) MultipartFile profileImage
    ) {
        MemberPkResponse memberPkResponse = memberCreateUseCase.createMember(memberCreateRequest, profileImage);

        return ApplicationResponse.onSuccess(memberPkResponse);
    }

    @CustomErrorCodes(
        memberErrorCodes = {
            INVALID_EMAIL, ALREADY_EXIST_EMAIL,
            INVALID_NAME,
            INVALID_NICKNAME, ALREADY_EXIST_NICKNAME,
            INVALID_DEPARTMENT,
            INVALID_POSITION,
            INVALID_PHONE,
            INVALID_ROLE
        },
        commonErrorCodes = {
            FILE_NOT_UPLOAD,
            INVALID_EXCEL_EXTENSION,
            EXCEED_MAX_FILE_SIZE
        }
    )
    @Operation(summary = "관리자 - 회원 등록(엑셀 파일 업로드)", description = "정해진 양식의 엑셀 파일 내용을 읽어 회원을 등록합니다.")
    @PostMapping(value = "/excel", consumes = MULTIPART_FORM_DATA_VALUE)
    public ApplicationResponse<Void> createMemberExcelUpload(
        @AuthenticationPrincipal Member member,
        @Parameter(description = "엑셀 파일")
        @RequestParam(required = false) MultipartFile file
    ) {
        List<MemberCreateRequestForExcel> memberCreateRequestForExcels = excelUtil.parseExcelToObject(file, MemberCreateRequestForExcel.class);
        memberCreateFromExcelUseCaseV3.createMembersWithEmailNotification(memberCreateRequestForExcels);

        return ApplicationResponse.onSuccess();
    }

    @Operation(summary = "관리자 - 회원 등록 엑셀 양식 다운로드", description = "회원 등록에 필요한 정해진 양식의 엑셀 파일을 다운로드합니다.")
    @CustomErrorCodes(commonErrorCodes = INTERNAL_SERVER_ERROR)
    @GetMapping(value = "/excel/example")
    public void createMemberExcelExampleDownload(
        HttpServletResponse response,
        @AuthenticationPrincipal Member member
    ) {
        List<MemberCreateRequestForExcel> memberInfoExamples = excelExampleCreateUseCase.createMemberInfoExample();
        excelUtil.renderObjectToExcel(response, memberInfoExamples, MemberCreateRequestForExcel.class, "member_info_example");
    }

    @CustomErrorCodes(
        memberErrorCodes = {
            MEMBER_NOT_FOUND,
            INVALID_EMAIL, ALREADY_EXIST_EMAIL,
            INVALID_NAME,
            INVALID_NICKNAME, ALREADY_EXIST_NICKNAME,
            INVALID_DEPARTMENT,
            INVALID_POSITION,
            INVALID_PHONE,
            INVALID_ROLE
        },
        commonErrorCodes = {
            INVALID_IMAGE_EXTENSION,
            EXCEED_MAX_FILE_SIZE
        }
    )
    @Operation(summary = "관리자 - 회원 정보 수정", description = "회원 정보를 수정합니다.")
    @PatchMapping(value = "/{memberId}", consumes = {MULTIPART_FORM_DATA_VALUE, APPLICATION_JSON_VALUE})
    public ApplicationResponse<MemberPkResponse> modifyMemberInfo(
        @AuthenticationPrincipal Member member,
        @PathVariable String memberId,
        @RequestPart(value = "request") @Valid MemberInfoUpdateRequest memberInfoUpdateRequest,
        @RequestPart(required = false) MultipartFile profileImage
    ) {
        MemberPkResponse memberPkResponse = memberInfoUpdateUseCase.modifyMemberInfo(memberId, memberInfoUpdateRequest, profileImage);
        return ApplicationResponse.onSuccess(memberPkResponse);
    }

    @CustomErrorCodes(memberErrorCodes = {MEMBER_NOT_FOUND})
    @Operation(summary = "관리자 - 선택한 회원 삭제", description = "선택한 회원을 일괄 삭제합니다.")
    @DeleteMapping
    public ApplicationResponse<Void> softDeleteMember(
        @AuthenticationPrincipal Member member,
        @RequestBody MemberDeleteRequest memberDeleteRequest
    ) {
        memberInfoUpdateUseCase.softDeleteMember(memberDeleteRequest.memberIdList());
        return ApplicationResponse.onSuccess();
    }

    @CustomErrorCodes(memberErrorCodes = {MEMBER_NOT_FOUND})
    @Operation(summary = "관리자 - 회원 정보 상세조회", description = "선택한 회원의 상세 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ApplicationResponse<MemberInfoResponse> getMemberInfo(@PathVariable String memberId) {
        MemberInfoResponse memberInfoDTO = memberInfoGetUseCase.getMemberInfo(memberId);
        return ApplicationResponse.onSuccess(memberInfoDTO);
    }

    @CustomErrorCodes(commonErrorCodes = {METHOD_ARGUMENT_NOT_VALID})
    @Operation(summary = "관리자 - 회원 정보 목록 조회 및 검색(페이징)", description = "회원 정보 목록을 페이징으로 조회합니다.")
    @Parameters({
        @Parameter(name = "role", description = "회원 역할 (USER | MANAGER)", example = "USER"),
        @Parameter(name = "query", description = "검색어 (아이디, 이메일, 이름, 부서)", example = "gachon.kim(아이디) or alsgudtkwjs@gachon.ac.kr(이메일) or 김가천(이름) or 부서(개발 1팀)"),
        @Parameter(name = "pageable", description = "페이징", example = "{\"page\":1,\"size\":20}")
    })
    @GetMapping
    public ApplicationResponse<ApplicationPageResponse<MemberInfoPreviewResponse>> getTotalMemberInfo(
        @AuthenticationPrincipal Member member,
        @Parameter(description = "페이징", example = "{\"page\":1,\"size\":20}")
        ApplicationPageRequest pageRequest,
        @RequestParam(required = false) Role role,
        @RequestParam(required = false) String query
    ) {
        return ApplicationResponse.onSuccess(
            memberInfoSearchUseCase.searchMemberInfo(
                pageRequest,
                role,
                query
            )
        );
    }
}

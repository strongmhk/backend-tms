package com.wrkr.tickety.domains.member.application.usecase;

import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.domain.constant.Role;
import com.wrkr.tickety.domains.member.domain.service.MemberSaveService;
import com.wrkr.tickety.domains.member.persistence.entity.MemberEntity;
import com.wrkr.tickety.domains.member.persistence.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
public class MemberSaveServicePerformanceTest {

    @MockitoBean
    private RedissonClient redissonClient;

    @Autowired
    private MemberSaveService memberSaveService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private List<MemberEntity> createTestMembers(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> MemberEntity.builder()
                .nickname("test.nick" + i)
                .password("Dummy0212*")
                .name("테스트" + i)
                .phone("010-1234-" + String.format("%04d", i))
                .email("test" + i + "@gachon.ac.kr")
                .department("DEPT")
                .position("POSITION")
                .profileImage("https://i.ibb.co/7Fd4Hhx/tickety-default-image.jpg")
                .role(Role.USER)
                .agitUrl("https://agit.example.com/" + i)
                .agitNotification(true)
                .emailNotification(true)
                .serviceNotification(true)
                .kakaoworkNotification(true)
                .isDeleted(false)
                .isTempPassword(true)
                .build()
            ).collect(Collectors.toList());
    }

    @AfterEach
    void cleanUp() {
//        entityManager.flush();
        memberRepository.deleteAllInBatch();
        entityManager.clear();
    }

    @Test
    void saveMemberTest() {
        List<MemberEntity> members = createTestMembers(1000);

        long start = System.currentTimeMillis();
        for (MemberEntity member : members) {
            memberSaveService.saveMember(member);
        }
        long end = System.currentTimeMillis();

        System.out.println("JPA save 단건 반복 저장 시간: " + (end - start) + "ms");
        Assertions.assertThat(memberRepository.count()).isEqualTo(1000);
    }

    @Test
    void saveAllMemberTest() {
        List<MemberEntity> members = createTestMembers(1000);

        long start = System.currentTimeMillis();
        memberSaveService.saveAllMember(members);
        long end = System.currentTimeMillis();

        System.out.println("JPA saveAll 저장 시간: " + (end - start) + "ms");
        Assertions.assertThat(memberRepository.count()).isEqualTo(1000)
        ;
    }

    @Test
    void bulkInsertMemberTest() {
        List<MemberCreateRequestForExcel> requests = createTestMembers(1000).stream()
            .map(member -> MemberCreateRequestForExcel.builder()
                .name(member.getName())
                .department(member.getDepartment())
                .position(member.getPosition())
                .phone(member.getPhone())
                .role(member.getRole())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .agitUrl(member.getAgitUrl())
                .build()
            )
            .collect(Collectors.toList());

        List<String> passwords = Collections.nCopies(1000, "icO8V*2hQIIC");

        long start = System.currentTimeMillis();
        memberSaveService.bulkInsertMember(requests, passwords);
        long end = System.currentTimeMillis();

        System.out.println("JDBC bulkInsert 저장 시간: " + (end - start) + "ms");
        Assertions.assertThat(memberRepository.count()).isEqualTo(1000);
    }

}

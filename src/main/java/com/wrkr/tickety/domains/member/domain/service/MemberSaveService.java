package com.wrkr.tickety.domains.member.domain.service;

import com.wrkr.tickety.domains.member.application.dto.request.MemberCreateRequestForExcel;
import com.wrkr.tickety.domains.member.domain.model.Member;
import com.wrkr.tickety.domains.member.persistence.adapter.MemberPersistenceAdapter;
import com.wrkr.tickety.domains.member.persistence.entity.MemberEntity;
import com.wrkr.tickety.domains.member.persistence.repository.MemberRepository;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSaveService {

    private final JdbcTemplate jdbcTemplate;
    private final MemberRepository memberRepository;
    private final MemberPersistenceAdapter memberPersistenceAdapter;

    @Transactional
    public Member save(Member member) {
        return memberPersistenceAdapter.save(member);
    }

    @Transactional
    public void saveMember(MemberEntity member) {
        memberRepository.save(member);
    }

    @Transactional
    public void saveAllMember(List<MemberEntity> members) {
        memberRepository.saveAll(members);
    }

    @Transactional
    public void bulkInsertMember(List<MemberCreateRequestForExcel> requests, List<String> encryptedPasswords) {
        String sql = """
        INSERT INTO member (
            nickname, password, name, phone, email,
            department, position, profile_image, role, agit_url,
            agit_notification, email_notification, service_notification,
            kakaowork_notification, is_deleted, is_temp_password, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MemberCreateRequestForExcel req = requests.get(i);
                String password = encryptedPasswords.get(i);

                ps.setString(1, req.getNickname());
                ps.setString(2, password);
                ps.setString(3, req.getName());
                ps.setString(4, req.getPhone());
                ps.setString(5, req.getEmail());
                ps.setString(6, req.getDepartment());
                ps.setString(7, req.getPosition());

                // 기본 이미지 처리
                String profileImage = req.getProfileImage() != null
                    ? req.getProfileImage()
                    : "https://i.ibb.co/7Fd4Hhx/tickety-default-image.jpg";
                ps.setString(8, profileImage);

                ps.setString(9, req.getRole().name());
                ps.setString(10, req.getAgitUrl());
                ps.setBoolean(11, true);  // agitNotification
                ps.setBoolean(12, true);  // emailNotification
                ps.setBoolean(13, true);  // serviceNotification
                ps.setBoolean(14, true);  // kakaoworkNotification
                ps.setBoolean(15, false); // isDeleted
                ps.setBoolean(16, true);  // isTempPassword
            }

            @Override
            public int getBatchSize() {
                return requests.size();
            }
        });
    }

}

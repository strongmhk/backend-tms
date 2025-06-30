package com.wrkr.tickety.domains.member.persistence.repository;

import com.wrkr.tickety.domains.member.domain.constant.Role;
import com.wrkr.tickety.domains.member.persistence.entity.MemberEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<MemberEntity, Long>, MemberQueryDslRepository {

    boolean existsByEmailAndIsDeleted(String email, Boolean isDeleted);

    boolean existsByNickname(String nickname);

    Optional<MemberEntity> findByNicknameAndIsDeleted(String nickname, boolean isDeleted);

    List<MemberEntity> findByRoleAndIsDeletedFalse(Role role);

    Optional<MemberEntity> findByMemberIdAndIsDeleted(Long memberId, boolean isDeleted);

    @Query("SELECT m.email FROM MemberEntity m WHERE m.email IN :emails AND m.isDeleted = false")
    List<String> findExistingEmails(@Param("emails") Set<String> emails);

    @Query("SELECT m.nickname FROM MemberEntity m WHERE m.nickname IN :nicknames AND m.isDeleted = false")
    List<String> findExistingNicknames(@Param("nicknames") Set<String> nicknames);

    @Query("SELECT m.memberId FROM MemberEntity m WHERE m.email = :email AND m.isDeleted = false")
    Long findIdByEmail(@Param("email") String email);

}

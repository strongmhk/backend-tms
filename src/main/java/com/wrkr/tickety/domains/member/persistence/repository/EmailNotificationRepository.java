package com.wrkr.tickety.domains.member.persistence.repository;

import com.wrkr.tickety.domains.member.domain.constant.EmailNotiStatus;
import com.wrkr.tickety.domains.member.persistence.entity.EmailNotificationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailNotificationRepository extends JpaRepository<EmailNotificationEntity, Long> {

    List<EmailNotificationEntity> findTop100ByStatusInAndRetryCountLessThan(List<EmailNotiStatus> statusList, int retryCount);
    List<EmailNotificationEntity> findByEmailIn(List<String> emails);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE EmailNotificationEntity e
        SET e.status = :status,
            e.retryCount = CASE WHEN :status = 'FAILED' THEN e.retryCount + 1 ELSE e.retryCount END
        WHERE e.email IN :emails
    """)
    void bulkUpdateStatusByEmails(
        @Param("status") EmailNotiStatus status,
        @Param("emails") List<String> emails
    );
}

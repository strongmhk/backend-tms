package com.wrkr.tickety.domains.member.persistence.entity;

import com.wrkr.tickety.domains.member.domain.constant.EmailNotiStatus;
import com.wrkr.tickety.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "email_notification")
public class EmailNotificationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailNotificationId;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String tempPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmailNotiStatus status;

    private int retryCount;

    public void markSent() {
        this.status = EmailNotiStatus.SENT;
    }

    public void markFailed() {
        this.status = EmailNotiStatus.FAILED;
        this.retryCount += 1;
    }
}

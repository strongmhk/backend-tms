package com.wrkr.tickety.domains.member.persistence.entity;

import com.wrkr.tickety.domains.member.domain.constant.Role;
import com.wrkr.tickety.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "member")
public class MemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String agitUrl;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean agitNotification;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean emailNotification;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean serviceNotification;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean kakaoworkNotification;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Boolean isDeleted;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Boolean isTempPassword;

    @Builder
    public MemberEntity(
        Long memberId,
        String nickname,
        String password,
        String name,
        String phone,
        String email,
        String department,
        String position,
        String profileImage,
        Role role,
        String agitUrl,
        Boolean agitNotification,
        Boolean emailNotification,
        Boolean serviceNotification,
        Boolean kakaoworkNotification,
        Boolean isDeleted,
        Boolean isTempPassword
    ) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.position = position;
        this.profileImage = profileImage;
        this.role = role;
        this.agitUrl = agitUrl;
        this.agitNotification = agitNotification;
        this.emailNotification = emailNotification;
        this.serviceNotification = serviceNotification;
        this.kakaoworkNotification = kakaoworkNotification;
        this.isDeleted = isDeleted;
        this.isTempPassword = isTempPassword;
    }
}

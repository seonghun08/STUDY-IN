package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.Address;
import com.studyIn.domain.account.NotificationSettings;
import com.studyIn.domain.account.dto.form.SignUpForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_authentication")
public class Authentication extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "authentication_id")
    private Long id;

    @OneToOne(mappedBy = "authentication")
    private Account account;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String cellPhone;

    private String birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Embedded
    private Address address;

    @Embedded
    private NotificationSettings notificationSettings;

    /**
     * emailVerified 이메일 인증 확인
     * emailVerifiedDate 이메일 인증 날짜
     * emailCheckToken 이메일 인증 토큰
     * emailCheckTokenGeneratedDate 이메일 인증 토큰 생성 날짜
     */
    private boolean emailVerified;
    private LocalDateTime emailVerifiedDate;
    private String emailCheckToken;
    private LocalDateTime emailCheckTokenGeneratedDate;


    //== 생성 메서드 ==//
    public static Authentication createAuthentication(SignUpForm form, NotificationSettings notificationSettings) {
        Authentication auth = new Authentication();
        auth.email = form.getEmail();
        auth.cellPhone = form.getCellPhone();
        auth.birthday = form.getBirthday();
        auth.gender = form.getGender();
        auth.notificationSettings = notificationSettings;
        return auth;
    }

    /**
     * 인증 토큰 UUID 생성
     */
    public void generateEmailToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedDate = LocalDateTime.now();
    }

    /**
     * 인증 토큰 일치 검증
     */
    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    /**
     * 이메일 인증 확인
     */
    public void confirmEmailAuthentication() {
        this.emailVerified = true;
        this.emailVerifiedDate = LocalDateTime.now();
    }

}

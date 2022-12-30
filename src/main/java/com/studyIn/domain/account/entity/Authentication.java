package com.studyIn.domain.account.entity;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.Address;
import com.studyIn.domain.account.NotificationSettings;
import com.studyIn.domain.account.form.SignUpForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_authentication")
public class Authentication {

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
     * email 인증 절차
     */
    private boolean emailVerified;
    private LocalDateTime emailVerifiedDate;
    private String emailCheckToken;

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

    public void generateEmailToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
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

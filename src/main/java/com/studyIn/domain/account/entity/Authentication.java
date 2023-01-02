package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.value.Address;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_authentication")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private NotificationsSetting notificationsSetting;

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
    public static Authentication createAuthentication(SignUpForm form, NotificationsSetting notificationsSetting) {
        Authentication auth = new Authentication();
        auth.email = form.getEmail();
        auth.cellPhone = form.getCellPhone();
        auth.birthday = form.getBirthday();
        auth.gender = form.getGender();
        auth.notificationsSetting = notificationsSetting;
        return auth;
    }


    //== 수정 메서드 ==//
    public void updateNotificationsSetting(NotificationsSettingForm notificationsSettingForm) {
        this.notificationsSetting.update(notificationsSettingForm);
    }


    /**
     * Email 인증 메서드
     * generateEmailToken 인증 토큰 UUID 생성
     * isValidToken 인증 토큰 일치 검증
     * confirmEmailAuthentication 인증 확인 완료
     * canSendConfirmEmail 이메일 전송 가능 여부
     */
    public void generateEmailToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedDate = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void confirmEmailAuthentication() {
        this.emailVerified = true;
        this.emailVerifiedDate = LocalDateTime.now();
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedDate.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}

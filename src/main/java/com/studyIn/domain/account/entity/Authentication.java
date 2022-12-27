package com.studyIn.domain.account.entity;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.Address;
import com.studyIn.domain.account.NotificationSettings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_authentication")
public class Authentication {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "authentication_id")
    private Long id;

    private String cellPhone;

    private String birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Embedded
    private Address address;

    @Embedded
    private NotificationSettings notificationSettings;

    public Authentication(String cellPhone, String birthday, Gender gender, Address address, NotificationSettings notificationSettings) {
        this.cellPhone = cellPhone;
        this.birthday = birthday;
        this.gender = gender;
        this.address = address;
        this.notificationSettings = notificationSettings;
    }

    private boolean emailVerified;

    private String emailCheckToken;
}

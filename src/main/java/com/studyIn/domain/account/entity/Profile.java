package com.studyIn.domain.account.entity;

import com.studyIn.domain.account.form.SignUpForm;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_profile")
public class Profile {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private Long id;

    private String nickname;

    private String bio;

    private String url;

    private String occupation;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String image;

    //== 생성 메서드 ==//
    public static Profile createProfile(SignUpForm form) {
        Profile profile = new Profile();
        profile.nickname = form.getNickname();
        return profile;
    }
}

package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.SignUpForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_profile")
public class Profile extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private Long id;

    @Column(nullable = false)
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

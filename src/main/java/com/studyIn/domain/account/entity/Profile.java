package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "account_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private Long id;

    @Column(nullable = false)
    private String nickname;

    private String bio;
    private String link;
    private String occupation;
    private String location;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String profileImage;

    //== 생성 메서드 ==//
    public static Profile createProfile(SignUpForm form) {
        Profile profile = new Profile();
        profile.nickname = form.getNickname();
        return profile;
    }

    //== 수정 메서드 ==//
    public void updateProfile(ProfileForm form) {
        this.nickname = form.getNickname();
        this.bio = form.getBio();
        this.link = form.getLink();
        this.occupation = form.getOccupation();
        this.location = form.getLocation();
        this.profileImage = form.getProfileImage();
    }
}

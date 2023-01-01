package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    //== 업데이트 메서드 ==//
    public void updateProfile(ProfileForm form) {
        this.nickname = form.getNickname();
        this.bio = form.getBio();
        this.link = form.getLink();
        this.occupation = form.getOccupation();
        this.location = form.getLocation();
        this.profileImage = form.getProfileImage();
    }
}

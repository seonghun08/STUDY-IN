package com.studyIn.domain.account.dto;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Lob;
import java.time.LocalDateTime;

@Data
public class ProfileDTO {

    /**
     * Account
     */
    private String username;
    private LocalDateTime createdDate;

    /**
     * Authentication
     */
    private String email;
    private boolean emailVerified;

    /**
     * Profile
     */
    private String nickname;
    private String occupation;
    private String location;
    private String bio;
    private String link;

    @Lob @Basic
    private String profileImage;

    /**
     * 프로필과 로그인 한 계정의 주인이 같는가
     */
    public boolean isOwner(String username) {
        return this.username.equals(username);
    }

    public ProfileDTO(String username, LocalDateTime createdDate, String email, boolean emailVerified,
                      String nickname, String occupation, String location, String bio, String link, String profileImage) {
        this.username = username;
        this.createdDate = createdDate;
        this.email = email;
        this.emailVerified = emailVerified;
        this.nickname = nickname;
        this.occupation = occupation;
        this.location = location;
        this.bio = bio;
        this.link = link;
        this.profileImage = profileImage;
    }
}

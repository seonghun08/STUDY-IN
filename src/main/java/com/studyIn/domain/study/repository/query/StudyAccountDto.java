package com.studyIn.domain.study.repository.query;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;

@Data
public class StudyAccountDto {

    private Long accountId;
    private String username;
    private String email;
    private String nickname;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String profileImage;

    private String bio;

    public StudyAccountDto(Long accountId, String username, String email, String nickname, String profileImage, String bio) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
    }
}
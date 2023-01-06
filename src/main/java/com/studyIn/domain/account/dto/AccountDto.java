package com.studyIn.domain.account.dto;

import com.studyIn.domain.account.entity.Account;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;

@Data
public class AccountDto {

    private Long accountId;
    private String username;
    private String email;
    private String nickname;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String profileImage;

    private String bio;

    public AccountDto(Account account) {
        this.username = account.getUsername();
        this.email = account.getProfile().getEmail();
        this.nickname = account.getProfile().getNickname();
        this.profileImage = account.getProfile().getProfileImage();
        this.bio = account.getProfile().getBio();
    }
}

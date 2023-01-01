package com.studyIn.domain.account;

import com.studyIn.domain.account.entity.Account;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Lob;
import java.time.LocalDateTime;

@Data
public class AccountInfo {

    /**
     * Account
     */
    private Long accountId;
    private String username;

    /**
     * Authentication
     */
    private Long authenticationId;
    private String email;
    private boolean emailVerified;

    /**
     * Profile
     */
    private Long profileId;
    private String profileImage;
    private String nickname;

    public AccountInfo(Account account) {
        this.accountId = account.getId();
        this.username = account.getUsername();

        this.authenticationId = account.getAuthentication().getId();
        this.email = account.getAuthentication().getEmail();
        this.emailVerified = account.getAuthentication().isEmailVerified();

        this.profileId = account.getProfile().getId();
        this.nickname = account.getProfile().getNickname();
        this.profileImage = account.getProfile().getProfileImage();
    }
}

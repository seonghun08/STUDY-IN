package com.studyIn.domain.account;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Profile;
import lombok.Data;

import java.io.Serializable;

@Data
public class AccountInfo implements Serializable {

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
    private String nickname;
    private String profileImage;

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

    /**
     * 프로필 수정 시 "DB"에서만 변경되기 때문에 세션도 같이 수정해야 한다.
     */
    public void updateProfile(Profile profile) {
        this.nickname = profile.getNickname();
        this.profileImage = profile.getProfileImage();
    }
}

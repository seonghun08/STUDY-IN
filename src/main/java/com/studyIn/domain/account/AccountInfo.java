package com.studyIn.domain.account;

import com.studyIn.domain.account.entity.Account;
import lombok.Data;
import net.bytebuddy.asm.Advice;

import javax.persistence.Basic;
import javax.persistence.Lob;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AccountInfo {

    private Long accountId;
    private String username;
    private String nickname;
    private String email;

    /**
     * 이메일 인증 확인
     */
    private boolean emailVerified;
    private LocalDateTime emailCheckTokenGeneratedDate;

    @Lob @Basic
    private String profileImage;

    public AccountInfo(Account account) {
        this.accountId = account.getId();
        this.username = account.getUsername();
        this.nickname = account.getProfile().getNickname();
        this.email = account.getAuthentication().getEmail();
        this.emailVerified = account.getAuthentication().isEmailVerified();
        this.emailCheckTokenGeneratedDate = account.getAuthentication().getEmailCheckTokenGeneratedDate();
        this.profileImage = account.getProfile().getImage();
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedDate.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}

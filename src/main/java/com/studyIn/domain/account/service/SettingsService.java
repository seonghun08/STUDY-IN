package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.AccountTag;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.tag.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SettingsService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 프로필 수정
     */
    public void updateProfile(ProfileForm profileForm, AccountInfo accountInfo) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId())
                .orElseThrow(IllegalArgumentException::new);

        profile.updateProfile(profileForm);
        accountInfo.updateProfile(profile);
    }

    /**
     * 패스워드 수정
     */
    public void updatePassword(PasswordForm passwordForm, AccountInfo accountInfo) {
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);

        account.updatePassword(passwordEncoder, passwordForm.getNewPassword());
    }

    /**
     * 알림 수정
     */
    public void updateNotificationsSetting(NotificationsSettingForm notificationsSettingForm, AccountInfo accountInfo) {
        Authentication authentication = authenticationRepository.findById(accountInfo.getAuthenticationId())
                .orElseThrow(IllegalArgumentException::new);

        authentication.updateNotificationsSetting(notificationsSettingForm);
    }

    /**
     * 주제 태그 수정 (추가)
     */
    public void addTag(Tag tag, AccountInfo accountInfo) {
        AccountTag accountTag = AccountTag.createAccountTag(tag);
        accountRepository.findById(accountInfo.getAccountId()).ifPresent(a -> a.addAccountTag(accountTag));
    }
}

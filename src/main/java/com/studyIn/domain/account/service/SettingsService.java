package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.UsernameForm;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
        profileRepository.findById(accountInfo.getProfileId())
                .ifPresent(profile -> {
                    profile.updateProfile(profileForm);
                    accountInfo.updateProfile(profile);
                });
    }

    /**
     * 패스워드 수정
     */
    public void updatePassword(PasswordForm passwordForm, AccountInfo accountInfo) {
        accountRepository.findById(accountInfo.getAccountId())
                .ifPresent(account ->
                        account.updatePassword(passwordEncoder, passwordForm.getNewPassword())
                );
    }

    /**
     * 알림 수정
     */
    public void updateNotificationsSetting(NotificationsSettingForm notificationsSettingForm, AccountInfo accountInfo) {
        authenticationRepository.findById(accountInfo.getAuthenticationId())
                .ifPresent(authentication ->
                        authentication.updateNotificationsSetting(notificationsSettingForm)
                );
    }

    /**
     * 주제 태그 수정 (추가)
     */
    public void addTag(Tag tag, AccountInfo accountInfo) {
        AccountTag accountTag = AccountTag.createAccountTag(tag);
        accountRepository.findById(accountInfo.getAccountId())
                .ifPresent(a -> a.addAccountTag(accountTag));
    }

    /**
     * 주제 태그 수정 (삭제)
     */
    public void removeTag(Tag tag, AccountInfo accountInfo) {
        /**
         * 수정하는 방식(추가, 제거)이 꼭 같아야 할까?
         * 추가하는 방식과 비슷하게 만들려면 쿼리가 더 많이 나가게 되는데... 뭐가 맞는지 모르겠다.
         * "JPA"는 테이블 구조를 객체에 맞춰서 도와주는 방식이다.
         * "DB" 입장에서 "accountTag"만 삭제만 하면 되는데, 굳이 "Account" 객체를 찾고 그 안에 값을 삭제해줘야 할까?
         * "Account" 객체를 트랜잭션 범위 안에 재사용한다면 모를까 굳이...?
         *
         * 현재 내가 더 좋은 방법을 못 찾는 것 일지도 모르겠다.
         * 일단 아는 범위 내에서는 이게 더 효율적이라고 판단해서 "DB"관점에서 해결하는 방식을 선택했다.
         *
         * orphanRemoval = true 사용 후,
         * accountRepository.findById(accountInfo.getAccountId()).ifPresent(a -> a.removeAccountTag(accountTag);
         */
        accountRepository.deleteAccountTag(tag.getId(), accountInfo.getAccountId());
    }

    /**
     * 지역 태그 수정 (추가)
     */
    public void addLocation(Location location, AccountInfo accountInfo) {
        AccountLocation accountLocation = AccountLocation.createAccountLocation(location);
        accountRepository.findById(accountInfo.getAccountId())
                .ifPresent(a -> a.addAccountLocation(accountLocation));
    }

    /**
     * 지역 태그 수정 (삭제)
     * 주제 태그 수정 (삭제)와 같이 고민이 되는데, 사실 쿼리가 가장 덜 나가는게 가장 좋은게 아닐까...? 합리화 중...
     */
    public void removeLocation(Location location, AccountInfo accountInfo) {
        accountRepository.deleteAccountLocation(location.getId(), accountInfo.getAccountId());
    }

    /**
     * 계정 수정
     */
    public void updateUsername(UsernameForm usernameForm, AccountInfo accountInfo) {
        accountRepository.findById(accountInfo.getAccountId())
                .ifPresent(account -> {
                    account.updateUsername(usernameForm.getUsername());
                    SecurityContextHolder.clearContext();
                });
    }
}

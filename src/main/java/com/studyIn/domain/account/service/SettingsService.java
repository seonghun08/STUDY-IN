package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SettingsService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateProfile(ProfileForm profileForm, AccountInfo accountInfo) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId())
                .orElseThrow(IllegalArgumentException::new);

        profile.updateProfile(profileForm);
        accountInfo.updateProfile(profile);
    }

    public void updatePassword(PasswordForm passwordForm, AccountInfo accountInfo) {
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);

        account.updatePassword(passwordEncoder, passwordForm.getNewPassword());
    }
}

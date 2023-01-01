package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.UserAccount;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SettingsService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;

    public void updateProfile(ProfileForm profileForm, AccountInfo accountInfo) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId())
                .orElseThrow(IllegalArgumentException::new);
        profile.updateProfile(profileForm);

        accountInfo.setProfileImage(profile.getProfileImage());
        accountInfo.setNickname(profile.getNickname());
    }
}

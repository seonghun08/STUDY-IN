package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class SettingsServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired SettingsService settingsService;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    private Account createAccount(SignUpForm form) {
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);
        return accountRepository.save(account);
    }
    private SignUpForm createSignUpForm(String username) {
        SignUpForm form = new SignUpForm();
        form.setUsername(username);
        form.setEmail(username + "@email.com");
        form.setPassword("1234567890");
        form.setNickname("nick-" + username);
        form.setCellPhone("01012341234");
        form.setGender(Gender.MAN);
        form.setBirthday("1997-08-30");
        return form;
    }

    @Test
    void updateProfile() {
        //given
        SignUpForm form = createSignUpForm("profile");
        Account account = createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        String bio = "안녕하세요";
        String nickname = "new-nickname";
        ProfileForm profileForm = new ProfileForm();
        profileForm.setBio(bio);
        profileForm.setNickname(nickname);

        //when
        settingsService.updateProfile(profileForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername()).orElseThrow();
        assertThat(findAccount.getProfile().getBio()).isEqualTo(bio);
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(nickname);
    }

    @Test
    void updatePassword() {
        //given
        SignUpForm form = createSignUpForm("password");
        Account account = createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        String password = "01010101010101";
        PasswordForm passwordForm = new PasswordForm();
        passwordForm.setCurrentPassword(form.getPassword());
        passwordForm.setNewPassword(password);
        passwordForm.setConfirmPassword(password);

        //when
        settingsService.updatePassword(passwordForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername()).orElseThrow();
        assertThat(passwordEncoder.matches(password, findAccount.getPassword())).isTrue();
    }

    @Test
    void updateNotificationsSetting() {
        //given
        SignUpForm form = createSignUpForm("password");
        Account account = createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        String password = "01010101010101";
        NotificationsSettingForm notificationsSettingForm = new NotificationsSettingForm();
        notificationsSettingForm.setStudyCreatedByEmail(true);
        notificationsSettingForm.setStudyUpdatedByEmail(true);
        notificationsSettingForm.setStudyEnrollmentResultByEmail(true);

        //when
        settingsService.updateNotificationsSetting(notificationsSettingForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername()).orElseThrow();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyCreatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyUpdatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyEnrollmentResultByEmail()).isTrue();
    }
}
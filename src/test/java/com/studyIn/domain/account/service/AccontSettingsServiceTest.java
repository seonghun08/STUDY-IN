package com.studyIn.domain.account.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.tag.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccontSettingsServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountService accountService;
    @Autowired
    AccontSettingsService accontSettingsService;
    @Autowired TagService tagService;
    @Autowired AccountRepository accountRepository;
    @Autowired TagRepository tagRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JPAQueryFactory jpaQueryFactory;

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
        accontSettingsService.updateProfile(profileForm, accountInfo);
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
        accontSettingsService.updatePassword(passwordForm, accountInfo);
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
        accontSettingsService.updateNotificationsSetting(notificationsSettingForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername()).orElseThrow();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyCreatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyUpdatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyEnrollmentResultByEmail()).isTrue();
    }

    @Test
    void addTag() {
        //given
        SignUpForm form = createSignUpForm("tag");
        Account account = createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring boot");
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);

        //when
        accontSettingsService.addTag(tag, accountInfo);
        em.flush();
        em.clear();

        //then
        AccountTag accountTag = jpaQueryFactory
                .selectFrom(QAccountTag.accountTag)
                .join(QAccountTag.accountTag.account, QAccount.account).fetchJoin()
                .join(QAccountTag.accountTag.tag, QTag.tag).fetchJoin()
                .where(QAccount.account.username.eq(account.getUsername()))
                .fetchOne();

        assertThat(accountTag.getTag().getTitle()).isEqualTo(tagForm.getTitle());
    }

    @Test
    void removeTag() {
        //given
        SignUpForm form = createSignUpForm("tag");
        Account account = createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring boot");
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);
        accontSettingsService.addTag(tag, accountInfo);
        em.flush();
        em.clear();

        //then
        accontSettingsService.removeTag(tag, accountInfo);

        //then
        AccountTag accountTag = jpaQueryFactory
                .selectFrom(QAccountTag.accountTag)
                .join(QAccountTag.accountTag.account, QAccount.account).fetchJoin()
                .join(QAccountTag.accountTag.tag, QTag.tag).fetchJoin()
                .where(QAccount.account.username.eq(account.getUsername()))
                .fetchOne();

        assertThat(accountTag == null).isTrue();
    }
}
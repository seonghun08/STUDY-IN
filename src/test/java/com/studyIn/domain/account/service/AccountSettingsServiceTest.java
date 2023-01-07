package com.studyIn.domain.account.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.*;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationForm;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.location.QLocation;
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
class AccountSettingsServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired AccontSettingsService accontSettingsService;
    @Autowired TagService tagService;
    @Autowired AccountRepository accountRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void updateProfile() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        String bio = "안녕하세요";
        String nickname = "new-nickname";
        ProfileForm profileForm = new ProfileForm();
        profileForm.setBio(bio);
        profileForm.setNickname(nickname);
        accontSettingsService.updateProfile(profileForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(account.getUsername())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(findAccount.getProfile().getBio()).isEqualTo(bio);
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(nickname);
    }

    @Test
    void updatePassword() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountFactory.createAccount(form);
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        String password = "01010101010101";
        PasswordForm passwordForm = new PasswordForm();
        passwordForm.setCurrentPassword(form.getPassword());
        passwordForm.setNewPassword(password);
        passwordForm.setConfirmPassword(password);
        accontSettingsService.updatePassword(passwordForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(passwordEncoder.matches(password, findAccount.getPassword())).isTrue();
    }

    @Test
    void updateNotificationsSetting() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        NotificationsSettingForm notificationsSettingForm = new NotificationsSettingForm();
        notificationsSettingForm.setStudyCreatedByEmail(true);
        notificationsSettingForm.setStudyUpdatedByEmail(true);
        notificationsSettingForm.setStudyEnrollmentResultByEmail(true);
        accontSettingsService.updateNotificationsSetting(notificationsSettingForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(account.getUsername())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyCreatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyUpdatedByEmail()).isTrue();
        assertThat(findAccount.getAuthentication().getNotificationsSetting().isStudyEnrollmentResultByEmail()).isTrue();
    }

    @Test
    void addTag() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring boot");
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);
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
        Account account = accountFactory.createAccount("spring-dev");
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
        assertThat(accountTag).isNull();
    }

    @Test
    void addLocation() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName("Seoul/(서울)/none");
        Location location = Location.createLocation(locationForm.getCityName(), locationForm.getLocalNameOfCity(), locationForm.getProvinceName());
        locationRepository.save(location);
        accontSettingsService.addLocation(location, accountInfo);
        em.flush();
        em.clear();

        //then
        AccountLocation accountLocation = jpaQueryFactory
                .selectFrom(QAccountLocation.accountLocation)
                .join(QAccountLocation.accountLocation.account, QAccount.account).fetchJoin()
                .join(QAccountLocation.accountLocation.location, QLocation.location).fetchJoin()
                .where(QAccount.account.username.eq(account.getUsername()))
                .fetchOne();
        assertThat(accountLocation.getLocation().toString()).isEqualTo(location.toString());
    }

    @Test
    void removeLocation() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName("Seoul/(서울)/none");
        Location location = Location.createLocation(locationForm.getCityName(), locationForm.getLocalNameOfCity(), locationForm.getProvinceName());
        locationRepository.save(location);
        accontSettingsService.addLocation(location, accountInfo);
        em.flush();
        em.clear();

        //when
        accontSettingsService.removeLocation(location, accountInfo);

        //then
        AccountLocation accountLocation = jpaQueryFactory
                .selectFrom(QAccountLocation.accountLocation)
                .join(QAccountLocation.accountLocation.account, QAccount.account).fetchJoin()
                .join(QAccountLocation.accountLocation.location, QLocation.location).fetchJoin()
                .where(QAccount.account.username.eq(account.getUsername()))
                .fetchOne();
        assertThat(accountLocation).isNull();
    }

    @Test
    void updateUsername() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        //when
        String username = "update-username";
        UsernameForm usernameForm = new UsernameForm();
        usernameForm.setUsername(username);
        accontSettingsService.updateUsername(usernameForm, accountInfo);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(findAccount.getUsername()).isEqualTo(username);
    }
}
package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.dto.form.SignUpForm;
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
class AuthenticationRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthenticationRepository authenticationRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Account createAccount(SignUpForm form) {
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);

        /* 임시 토근 생성 */
        account.getAuthentication().generateEmailToken();
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
    void existsByEmail() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dev");

        String email = "user@email.com";
        form.setEmail(email);
        Authentication authentication = Authentication.createAuthentication(form, new NotificationsSetting());

        //when
        authenticationRepository.save(authentication);
        em.flush();
        em.clear();

        //then
        assertThat(authenticationRepository.existsByEmail(email)).isTrue();
    }

    @Test
    void findNotificationsSettingById() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dev");
        Account account = createAccount(form);
        em.flush();
        em.clear();

        //when
        NotificationsSetting notificationsSetting = authenticationRepository
                .findNotificationsSettingById(account.getAuthentication().getId()).orElseThrow();

        //then
        assertThat(notificationsSetting.isStudyCreatedByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyEnrollmentResultByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyUpdatedByWeb()).isTrue();
    }

}
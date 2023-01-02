package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.dto.form.SignUpForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AuthenticationRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired AuthenticationRepository authenticationRepository;
    @Autowired AccountService accountService;

    @Test
    public void existsByEmail() throws Exception {
        //given
        String email = "user@email.com";
        SignUpForm signUpForm = getSignUpForm(email);
        Authentication authentication = Authentication.createAuthentication(signUpForm, new NotificationsSetting());

        //when
        authenticationRepository.save(authentication);
        em.flush();
        em.clear();

        //then
        assertThat(authenticationRepository.existsByEmail(email)).isTrue();
    }

    @Test
    public void findNotificationsSettingById() throws Exception {
        //given
        SignUpForm form = getSignUpForm("user@email.com");
        Account account = accountService.signUp(form);
        em.flush();
        em.clear();

        //when
        NotificationsSetting notificationsSetting = authenticationRepository.findNotificationsSettingById(account.getAuthentication().getId())
                .orElseThrow();

        //then
        assertThat(notificationsSetting.isStudyCreatedByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyEnrollmentResultByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyUpdatedByWeb()).isTrue();
    }

    private SignUpForm getSignUpForm(String email) {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("username");
        signUpForm.setEmail(email);
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("1997-08-30");
        return signUpForm;
    }
}
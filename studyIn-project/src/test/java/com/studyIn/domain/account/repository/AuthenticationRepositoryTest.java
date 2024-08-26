package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.service.AccountService;
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
    @Autowired AuthenticationRepository authenticationRepository;
    @Autowired AccountFactory accountFactory;

    @Test
    void existsByEmail() throws Exception {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        form.setEmail("test@emai.com");
        accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        assertThat(authenticationRepository.existsByEmail(form.getEmail())).isTrue();
    }

    @Test
    void findNotificationsSettingById() throws Exception {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        form.setEmail("test@emai.com");
        Account account = accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        NotificationsSetting notificationsSetting = authenticationRepository
                .findNotificationsSettingById(account.getAuthentication().getId())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(notificationsSetting.isStudyCreatedByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyEnrollmentResultByWeb()).isTrue();
        assertThat(notificationsSetting.isStudyUpdatedByWeb()).isTrue();
    }
}
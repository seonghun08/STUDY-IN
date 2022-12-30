package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.NotificationSettings;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.form.SignUpForm;
import com.studyIn.domain.account.service.AccountService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class AuthenticationRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired AuthenticationRepository authenticationRepository;

    @Test
    public void existsByEmail() throws Exception {
        //given
        String email = "user@email.com";
        SignUpForm signUpForm = getSignUpForm(email);
        Authentication authentication = Authentication.createAuthentication(signUpForm, new NotificationSettings());

        //when
        authenticationRepository.save(authentication);
        em.flush();
        em.clear();

        //then
        assertThat(authenticationRepository.existsByEmail(email)).isTrue();
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
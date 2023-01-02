package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.dto.form.SignUpForm;
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
class AccountRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountRepository accountRepository;
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
    void existsByUsername() throws Exception {
        //given
        String username = "user";
        SignUpForm form = createSignUpForm(username);

        //when
        createAccount(form);
        em.flush();
        em.clear();

        //then
        assertThat(accountRepository.existsByUsername(username)).isTrue();
    }

    @Test
    void findByUsername() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dev");

        //when
        createAccount(form);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByUsername(form.getUsername()).orElseThrow();

        /**
         * Lazy 로딩이 일어나면 안된다.
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }

    @Test
    void findByEmail() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dec");

        //when
        createAccount(form);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findByEmail(form.getEmail()).orElseThrow();

        /**
         * Lazy 로딩이 일어나면 안된다.
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }
}
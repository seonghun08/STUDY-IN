package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;

    @Test
    void existsByUsername() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        assertThat(accountRepository.existsByUsername(form.getUsername())).isTrue();
    }

    @Test
    void findByUsername() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        Account findAccount = accountRepository.findByUsername(form.getUsername())
                .orElseThrow(IllegalArgumentException::new);
        /**
         * Lazy 로딩이 일어나면 안된다.
         * Account -> Authentication
         * Account -> Profile
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }

    @Test
    void findByEmail() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        Account findAccount = accountRepository.findByEmail(form.getEmail())
                .orElseThrow(IllegalArgumentException::new);
        /**
         * Lazy 로딩이 일어나면 안된다.
         * Account -> Authentication
         * Account -> Profile
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }

    @Test
    void findWithAuthenticationById() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        Account findAccount = accountRepository.findWithAuthenticationById(account.getId())
                .orElseThrow(IllegalArgumentException::new);
        /**
         * Lazy 로딩이 일어나면 안된다.
         * Account -> Authentication
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
    }

    @Test
    void findWithAuthenticationByEmail() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when, then
        Account findAccount = accountRepository.findWithAuthenticationByEmail(form.getEmail())
                .orElseThrow(IllegalArgumentException::new);
        /**
         * Lazy 로딩이 일어나면 안된다.
         * Account -> Authentication
         */
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
    }
}
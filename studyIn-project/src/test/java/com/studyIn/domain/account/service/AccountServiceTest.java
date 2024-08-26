package com.studyIn.domain.account.service;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.dto.form.EmailForm;
import com.studyIn.domain.account.dto.form.ResetPasswordForm;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@Transactional
@SpringBootTest
class AccountServiceTest {

    @PersistenceContext EntityManager em;
    @MockBean EmailService emailService;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void signUp() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");

        //when
        Account account = accountService.signUp(form);
        em.flush();
        em.clear();

        //then
        Account findAccount = em.createQuery(
                        "select a from Account a" +
                                " join fetch a.authentication au" +
                                " join fetch a.profile p" +
                                " where a.id = :id", Account.class
                )
                .setParameter("id", account.getId())
                .getSingleResult();
        /**
         * PasswordEncoder => "SignUpForm"에 넣은 패스워드와 DB에 저장된 패스워드는 서로 일치하면 안된다.
         * 이외에 "SignUpForm"과 DB에 불러온 값과 서로 일치해야 한다.
         * 인증 토큰이 존재해야 한다.
         */
        assertThat(findAccount.getUsername()).isEqualTo(form.getUsername());
        assertThat(findAccount.getPassword()).isNotEqualTo(form.getPassword());
        assertThat(findAccount.getAuthentication().getEmailCheckToken()).isNotEmpty();
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getAuthentication().getGender()).isEqualTo(form.getGender());
        assertThat(findAccount.getAuthentication().getBirthday()).isEqualTo(form.getBirthday());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
        then(emailService).should().send(any(EmailMessage.class));
    }

    @Test
    void completeSignUp() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountService.signUp(form);

        String username = account.getUsername();
        String email = account.getAuthentication().getEmail();
        String token = account.getAuthentication().getEmailCheckToken();

        //when
        assertThat(accountService.completeSignUp(email, token)).isTrue();
        em.flush();
        em.clear();

        //then
        Account updateAccount = accountRepository.findByUsername(username)
                .orElseThrow(IllegalArgumentException::new);
        /**
         * completeSignUp => email, token 값이 정상적으로 들어갔다면 인증 완료 및 인증 완료일 업데이트가 되어야 한다.
         */
        assertThat(updateAccount.getAuthentication().isEmailVerified()).isTrue();
        assertThat(updateAccount.getAuthentication().getEmailVerifiedDate()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void sendSignUpConfirmEmail() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountService.signUp(form);

        String email = form.getEmail();
        String oldToken = account.getAuthentication().getEmailCheckToken();
        LocalDateTime oldTokenDate = account.getAuthentication().getEmailCheckTokenGeneratedDate();

        //when, then
        accountService.sendSignUpConfirmEmail(account.getId());
        Account findAccount = accountRepository.findByEmail(email)
                .orElseThrow(IllegalArgumentException::new);

        String newToken = findAccount.getAuthentication().getEmailCheckToken();
        LocalDateTime newTokenDate = findAccount.getAuthentication().getEmailCheckTokenGeneratedDate();

        assertThat(oldToken).isNotEqualTo(newToken);
        assertThat(oldTokenDate).isBefore(newTokenDate);
    }

    @Test
    void sendLoginLinkConfirmEmail() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountService.signUp(form);

        String email = form.getEmail();
        String oldToken = account.getAuthentication().getEmailCheckToken();
        LocalDateTime oldTokenDate = account.getAuthentication().getEmailCheckTokenGeneratedDate();

        //when, then
        EmailForm emailForm = new EmailForm();
        emailForm.setEmail(form.getEmail());
        accountService.sendLoginLinkConfirmEmail(emailForm);

        Account findAccount = accountRepository.findByEmail(email)
                .orElseThrow(IllegalArgumentException::new);

        String newToken = findAccount.getAuthentication().getEmailCheckToken();
        LocalDateTime newTokenDate = findAccount.getAuthentication().getEmailCheckTokenGeneratedDate();

        assertThat(oldToken).isNotEqualTo(newToken);
        assertThat(oldTokenDate).isBefore(newTokenDate);
    }

    @Test
    void resetPassword() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        Account account = accountService.signUp(form);
        String email = form.getEmail();

        //when
        String newPassword = "0101010101010";
        ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setEmail(email);
        resetPasswordForm.setPassword(newPassword);
        resetPasswordForm.setConfirmPassword(newPassword);
        accountService.resetPassword(resetPasswordForm);
        em.flush();
        em.clear();

        // then
        Account findAccount = accountRepository.findByEmail(email)
                .orElseThrow(IllegalArgumentException::new);
        assertThat(passwordEncoder.matches(newPassword, findAccount.getPassword())).isTrue();
    }
}
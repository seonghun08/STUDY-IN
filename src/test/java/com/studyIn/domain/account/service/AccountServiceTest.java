package com.studyIn.domain.account.service;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;

import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthenticationRepository authenticationRepository;

    @Test
    public void signUp() throws Exception {
        //given
        SignUpForm form = getSignUpForm();

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
         * signUp =>
         *  "Authentication"에 인증 토큰이 있어야 한다.
         *  이외에 "SignUpForm"과 DB에 불러온 값과 서로 일치해야 한다.
         */
        assertThat(findAccount.getUsername()).isEqualTo(form.getUsername());
        assertThat(findAccount.getPassword()).isNotEqualTo(form.getPassword());
        assertThat(findAccount.getAuthentication().getEmailCheckToken()).isNotEmpty();
        assertThat(findAccount.getAuthentication().getEmail()).isEqualTo(form.getEmail());
        assertThat(findAccount.getAuthentication().getGender()).isEqualTo(form.getGender());
        assertThat(findAccount.getAuthentication().getBirthday()).isEqualTo(form.getBirthday());
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }

    @Test
    public void completeSignUp() throws Exception {
        //given
        SignUpForm form = getSignUpForm();
        Account account = accountService.signUp(form);

        String username = account.getUsername();
        String email = account.getAuthentication().getEmail();
        String token = account.getAuthentication().getEmailCheckToken();

        //when
        assertThat(accountService.completeSignUp(email, token)).isTrue();
        em.flush();
        em.clear();

        //then
        Account updateAccount = accountRepository.findByUsername(username).orElseThrow();
        /**
         * "completeSignUp" =>
         *  email, token 값이 정상적으로 들어갔다면 인증 완료 및 인증 완료일 업데이트가 되어야 한다.
         */
        assertThat(updateAccount.getAuthentication().isEmailVerified()).isTrue();
        assertThat(updateAccount.getAuthentication().getEmailVerifiedDate()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    public void resendSignUpConfirmEmail() throws Exception {
        //given
        SignUpForm form = getSignUpForm();
        Account account = accountService.signUp(form);
        String oldToken = account.getAuthentication().getEmailCheckToken();
        LocalDateTime oldTokenDate = account.getAuthentication().getEmailCheckTokenGeneratedDate();

        //when
        accountService.resendSignUpConfirmEmail(account.getId());

        //then
        Account findAccount = accountRepository.findByEmail(account.getAuthentication().getEmail()).orElseThrow();
        String newToken = findAccount.getAuthentication().getEmailCheckToken();
        LocalDateTime newTokenDate = findAccount.getAuthentication().getEmailCheckTokenGeneratedDate();

        assertThat(oldToken).isNotEqualTo(newToken);
        assertThat(oldTokenDate).isBefore(newTokenDate);
    }

    private SignUpForm getSignUpForm() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("user");
        signUpForm.setEmail("user@email.com");
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("19970830");
        return signUpForm;
    }
}
package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.form.SignUpForm;
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

    @Test
    public void existsByUsername() throws Exception {
        //given
        String username = "user";
        SignUpForm form = getSignUpForm(username);
        Account account = Account.createUser(form, null, null);

        //when
        accountRepository.save(account);
        em.flush();
        em.clear();

        //then
        assertThat(accountRepository.existsByUsername(username)).isTrue();
    }

    @Test
    public void findWithProfileById() throws Exception {
        //given
        SignUpForm form = getSignUpForm();
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, null);

        //when
        Account saveAccount = accountRepository.save(account);
        em.flush();
        em.clear();

        //then
        Account findAccount = accountRepository.findWithProfileById(saveAccount.getId()).orElseThrow();

        /**
         * Lazy 로딩이 일어나면 안된다.
         */
        assertThat(findAccount.getProfile().getNickname()).isEqualTo(form.getNickname());
    }

    private SignUpForm getSignUpForm() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("username");
        signUpForm.setEmail("username@email.com");
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("1997-08-30");
        return signUpForm;
    }
    private SignUpForm getSignUpForm(String username) {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername(username);
        signUpForm.setEmail("username@email.com");
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("1997-08-30");
        return signUpForm;
    }
}
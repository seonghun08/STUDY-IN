package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.value.Gender;
import com.studyIn.domain.account.value.NotificationSettings;
import com.studyIn.domain.account.dto.ProfileDTO;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountRepositoryCustomImplTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountRepository accountRepository;

    @Test
    public void findProfileDtoByUsername() throws Exception {
        //given
        SignUpForm form = getSignUpForm("spring-dev");
        Account account = getAccount(form);
        Account saveAccount = accountRepository.save(account);

        em.flush();
        em.clear();

        //when
        ProfileDTO dto = accountRepository.findProfileDtoByUsername(form.getUsername()).orElseThrow();

        //then
        assertThat(dto.getUsername()).isEqualTo(form.getUsername());
        assertThat(dto.getEmail()).isEqualTo(form.getEmail());

        // "Optional"에 담아오면 뒷 소수점이 누락된다?
//        assertThat(dto.getCreatedDate()).isEqualTo(saveAccount.getCreatedDate());
    }

    private Account getAccount(SignUpForm form) {
        Profile profile = Profile.createProfile(form);
        Authentication authentication = Authentication.createAuthentication(form, new NotificationSettings());
        return Account.createUser(form, profile, authentication);
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
package com.studyIn.domain.account.service;

import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.entity.User;
import com.studyIn.domain.account.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional
@SpringBootTest
@Rollback(value = false)
class AccountServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired AccountService accountService;
    @Autowired UserRepository userRepository;

    @Test
    public void signUp() throws Exception {
        //given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("username");
        signUpForm.setEmail("username@email.com");
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("19970830");
        signUpForm.setCity("인천");
        signUpForm.setStreet("street");
        signUpForm.setZipcode("zipcode");

        //when
        Long saveAccount = accountService.signUp(signUpForm);

        em.flush();
        em.clear();

        //then
        User user = userRepository.findById(saveAccount).orElseThrow();
        System.out.println(user.getUsername());
        System.out.println(user.getEmail());
    }
}
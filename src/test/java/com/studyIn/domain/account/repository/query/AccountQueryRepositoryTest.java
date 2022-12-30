package com.studyIn.domain.account.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.Gender;
import com.studyIn.domain.account.dto.AccountDTO;
import com.studyIn.domain.account.form.SignUpForm;
import com.studyIn.domain.account.service.AccountService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class AccountQueryRepositoryTest {

    @Autowired AccountQueryRepository accountQueryRepository;
    @Autowired AccountService accountService;

    @Test
    public void findCountAndNicknameByEmail() throws Exception {
        //given
        SignUpForm form = getSignUpForm();
        accountService.signUp(form);

        //when
        AccountDTO dto = accountQueryRepository.findCountAndNicknameByEmail(form.getEmail());

        //then
        assertThat(dto.getNickname()).isEqualTo("nickname");
        assertThat(dto.getTotalCount()).isEqualTo(1);
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
package com.studyIn.domain.account.repository.query;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.dto.TotalCountAndNicknameDto;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountQueryRepositoryTest {

    @Autowired AccountFactory accountFactory;
    @Autowired AccountQueryRepository accountQueryRepository;

    @Test
    void findCountAndNicknameByEmail() throws Exception {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        form.setNickname("new-nickname");
        accountFactory.createAccount(form);

        //when
        TotalCountAndNicknameDto dto = accountQueryRepository.findCountAndNicknameByEmail(form.getEmail());

        //then
        assertThat(dto.getNickname()).isEqualTo("new-nickname");
        assertThat(dto.getTotalCount()).isEqualTo(1);
    }
}
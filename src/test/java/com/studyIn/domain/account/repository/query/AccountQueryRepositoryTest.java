package com.studyIn.domain.account.repository.query;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.dto.TotalCountAndNicknameDto;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountQueryRepositoryTest {

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountQueryRepository accountQueryRepository;
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
    void findCountAndNicknameByEmail() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dev");
        form.setNickname("new-nickname");
        createAccount(form);

        //when
        TotalCountAndNicknameDto dto = accountQueryRepository.findCountAndNicknameByEmail(form.getEmail());

        //then
        assertThat(dto.getNickname()).isEqualTo("new-nickname");
        assertThat(dto.getTotalCount()).isEqualTo(1);
    }

}
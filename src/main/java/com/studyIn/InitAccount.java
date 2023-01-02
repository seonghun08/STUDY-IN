package com.studyIn;

import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.entity.value.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitAccount {

    private final InitAccountService initAccountService;

    @PostConstruct
    public void init() {
        initAccountService.init();
    }

    /**
     * @PostConstruct
     * @Transactional
     * 스프링 "lifeCycle"에서 서로 같이 놓으면 떄문에 분리해줘야 한다.
     */
    @Component
    @RequiredArgsConstructor
    static class InitAccountService {

        private final AccountService accountService;

        @Transactional
        public void init() {
            SignUpForm form = new SignUpForm();
            form.setUsername("shajh2210");
            form.setEmail("shajh2210@gmail.com");
            form.setPassword("123123123");
            form.setNickname("seonghun_");
            form.setCellPhone("01066064349");
            form.setGender(Gender.MAN);
            form.setBirthday("1997-08-30");
            accountService.signUp(form);
        }
    }
}

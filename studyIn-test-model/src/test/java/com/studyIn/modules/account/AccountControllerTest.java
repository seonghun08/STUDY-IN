package com.studyIn.modules.account;

import com.studyIn.infra.AbstractContainerBaseTest;
import com.studyIn.infra.MockMvcTest;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;

    @MockBean EmailService emailService;

    @DisplayName("회원가입 화면이 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_fail() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("nickname", "nickname")
                        .param("username", "username")
                        .param("email", "email...")
                        .param("genderType", String.valueOf(GenderType.none))
                        .param("birthday", "1997-08-30")
                        .param("password", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_success() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("nickname", "nickname")
                        .param("username", "username")
                        .param("email", "email@studyIn.com")
                        .param("genderType", String.valueOf(GenderType.man))
                        .param("birthday", "1997-08-30")
                        .param("password", "1234567890")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("username"));

        Account account = accountRepository.findByEmail("email@studyIn.com");
        assertNotNull(account.getEmailCheckToken());
        assertNotEquals("1234567890", account.getPassword());
        assertTrue(accountRepository.existsByNickname("nickname"));
        assertTrue(accountRepository.existsByEmail("email@studyIn.com"));
        then(emailService).should().send(any(EmailMessage.class));
    }

    @DisplayName("인증 메인 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        accountFactory.createAccount("test");
        mvc.perform(get("/check-email-token")
                        .param("token", "dh6nsf24dsf1ds")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메인 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {

        Account account = new Account();
        account.setUsername("user");
        account.setNickname("nickname");
        account.setEmail("email@studyIn.com");
        account.setPassword("1234567890");

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();
        System.out.println(":: newAccount.getEmailCheckToken() = " + newAccount.getEmailCheckToken());

        mvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());
    }
}
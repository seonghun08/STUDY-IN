package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.value.Gender;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @MockBean JavaMailSender javaMailSender;

    @Autowired MockMvc mvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthenticationRepository authenticationRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("user");
        signUpForm.setEmail("user@email.com");
        signUpForm.setPassword("1234567890");
        signUpForm.setNickname("nickname");
        signUpForm.setCellPhone("01012341234");
        signUpForm.setGender(Gender.MAN);
        signUpForm.setBirthday("1997-08-30");
        accountService.signUp(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("회원 가입 화면이 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mvc.perform(get("/sign-up"))
                .andDo(print()) // view templates 랜더링을 볼 수 있다.
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUp_fail() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("username", "username")
                        .param("email", "xxxx")
                        .param("password", "1234567890")
                        .param("nickname", "nickname")
                        .param("gender", String.valueOf(Gender.MAN))
                        .param("birthday", "1997-08-30")
                        .param("cellPhone", "01012341234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUp_success() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("username", "username")
                        .param("email", "email@studyIn.com")
                        .param("password", "1234567890")
                        .param("nickname", "nickname")
                        .param("gender", String.valueOf(Gender.MAN))
                        .param("birthday", "1997-08-30")
                        .param("cellPhone", "01012341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated());

        Account account = accountRepository.findByUsername("username").orElseThrow();
        assertThat(account.getPassword()).isNotEqualTo("1234567890");
        assertThat(passwordEncoder.matches("1234567890", account.getPassword())).isTrue();
        assertThat(account.getAuthentication().getEmail()).isEqualTo("email@studyIn.com");
        assertThat(account.getProfile().getNickname()).isEqualTo("nickname");
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("email 인증 확인 - 입력값 오류")
    @Test
    void checkEmailToken_fail() throws Exception {
        Account account = createAccount();
        System.out.println(":: emailCheckToken = " + account.getAuthentication().getEmailCheckToken());

        mvc.perform(get("/check-email-token")
                        .param("token", "dh6nsf24dsf1ds")
                        .param("email", "xxxx@xxxx.xxx"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("email 인증 확인 - 입력값 정상")
    @Test
    void checkEmailToken_success() throws Exception {
        Account account = createAccount();
        System.out.println(":: emailCheckToken = " + account.getAuthentication().getEmailCheckToken());

        mvc.perform(get("/check-email-token")
                        .param("email", account.getAuthentication().getEmail())
                        .param("token", account.getAuthentication().getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이메일 재전송 화면 보이는지 테스트")
    @Test
    public void checkEmail() throws Exception {
        mvc.perform(get("/check-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("email"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이메일 재전송은 1~2분마다 한번만 가능 - 실패")
    @Test
    public void resendConfirmEmail_fail() throws Exception {
        mvc.perform(get("/resend-confirm-email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"))
                .andExpect(view().name("account/check-email"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 화면 보이는지 테스트")
    @Test
    public void viewProfile() throws Exception {
        String username = "user";
        mvc.perform(get("/profile/" + username))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(view().name("account/profile"));
    }

    private Account createAccount() {
        SignUpForm form = new SignUpForm();
        form.setUsername("user");
        form.setEmail("user@email.com");
        form.setPassword("1234567890");
        form.setNickname("nickname");
        form.setCellPhone("01012341234");
        form.setGender(Gender.MAN);
        form.setBirthday("19970830");
        return accountService.signUp(form);
    }
}
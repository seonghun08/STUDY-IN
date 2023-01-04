package com.studyIn.domain.account.controller;

import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

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

    @MockBean EmailService emailService;
    @Autowired MockMvc mvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired AuthenticationRepository authenticationRepository;
    @Autowired AuthenticationManager authenticationManager;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        SignUpForm form = createSignUpForm("user");
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);

        /* 임시 토근 생성 */
        account.getAuthentication().generateEmailToken();
        accountRepository.save(account);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

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

    @DisplayName("회원가입 실패 - 입력값 오류")
    @Test
    void signUp_fail() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("username", "new-username")
                        .param("email", "xxxx")
                        .param("password", "1234567890")
                        .param("nickname", "new-nickname")
                        .param("gender", String.valueOf(Gender.MAN))
                        .param("birthday", "1997-08-30")
                        .param("cellPhone", "01012341234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 성공")
    @Test
    void signUp_success() throws Exception {
        mvc.perform(post("/sign-up")
                        .param("username", "new-username")
                        .param("email", "email@new.com")
                        .param("password", "1234567890")
                        .param("nickname", "new-nickname")
                        .param("gender", String.valueOf(Gender.NONE))
                        .param("birthday", "1997-08-30")
                        .param("cellPhone", "01012341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated());

        Account account = accountRepository.findByUsername("new-username").orElseThrow();
        assertThat(account.getPassword()).isNotEqualTo("1234567890");
        assertThat(passwordEncoder.matches("1234567890", account.getPassword())).isTrue();
        assertThat(account.getAuthentication().getEmail()).isEqualTo("email@new.com");
        assertThat(account.getProfile().getNickname()).isEqualTo("new-nickname");
        then(emailService).should().send(any(EmailMessage.class));
    }

    @DisplayName("email 인증 실패 - 입력값 오류")
    @Test
    void checkEmailToken_fail() throws Exception {
        SignUpForm form = createSignUpForm("test");
        Account account = createAccount(form);
        System.out.println(":: emailCheckToken = " + account.getAuthentication().getEmailCheckToken());

        mvc.perform(get("/check-email-token")
                        .param("email", "xxxx@xxxx.xxx")
                        .param("token", "dh6nsf24dsf1ds"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("email 인증 성공")
    @Test
    void checkEmailToken_success() throws Exception {
        SignUpForm form = createSignUpForm("test");
        Account account = createAccount(form);
        System.out.println(":: emailCheckToken = " + account.getAuthentication().getEmailCheckToken());

        mvc.perform(get("/check-email-token")
                        .param("email", account.getAuthentication().getEmail())
                        .param("token", account.getAuthentication().getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이메일 재전송 화면 보이는지 테스트")
    @Test
    void checkEmail() throws Exception {
        mvc.perform(get("/check-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("email"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이메일 재전송 실패 - 이메일 재전송은 1~2분마다 한번만 가능")
    @Test
    void resendConfirmEmail_fail() throws Exception {
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
    void viewProfile() throws Exception {
        String username = "user";
        mvc.perform(get("/profile/" + username))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(view().name("account/profile"));
    }

    @DisplayName("패스워드 찾기 화면 보이는지 테스트")
    @Test
    void findPasswordForm() throws Exception {
        mvc.perform(get("/find-password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("emailForm"))
                .andExpect(view().name("account/find-password"));
    }

    @DisplayName("패스워드 찾기 이메일 실패 - 이메일 재전송은 1~2분마다 한번만 가능")
    @Test
    void findPassword() throws Exception {
        mvc.perform(post("/find-password")
                        .param("email", "user@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"));
    }

    @DisplayName("email 인증 실패 시, 패스워드 변경 불가")
    @Test
    void resetPasswordForm_fail() throws Exception {
        SignUpForm form = createSignUpForm("test");
        Account account = createAccount(form);

        mvc.perform(get("/find-by-email")
                        .param("email", account.getAuthentication().getEmail())
                        .param("token", "xxxx"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("resetPasswordForm"))
                .andExpect(view().name("account/find-by-email"));
    }

    @DisplayName("email 인증 성공 후, 패스워드 변경 화면 보이는지 테스트")
    @Test
    void resetPasswordForm_success() throws Exception {
        SignUpForm form = createSignUpForm("test");
        Account account = createAccount(form);

        mvc.perform(get("/find-by-email")
                        .param("email", account.getAuthentication().getEmail())
                        .param("token", account.getAuthentication().getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("resetPasswordForm"))
                .andExpect(view().name("account/find-by-email"));
    }
}
package com.studyIn.domain.main;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired MockMvc mvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        SignUpForm form = createSignUpForm();
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);
        accountRepository.save(account);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    private SignUpForm createSignUpForm() {
        SignUpForm form = new SignUpForm();
        form.setUsername("user");
        form.setEmail("user@email.com");
        form.setPassword("1234567890");
        form.setNickname("nickname");
        form.setCellPhone("01012341234");
        form.setGender(Gender.MAN);
        form.setBirthday("1997-08-30");
        return form;
    }

    @DisplayName("로그인 없이 home 화면")
    @Test
    public void home() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("accountInfo"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("로그인 시 home 화면")
    @Test
    public void login_home() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(authenticated().withUsername("user"));
    }

    @DisplayName("username 로그인 성공")
    @Test
    public void login_with_username() throws Exception {
        mvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "1234567890")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user"));
    }

    @DisplayName("email 로그인 성공")
    @Test
    public void login_with_email() throws Exception {
        mvc.perform(post("/login")
                        .param("username", "user@email.com")
                        .param("password", "1234567890")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user"));
    }

    @DisplayName("로그인 실패")
    @Test
    public void login_fail() throws Exception {
        mvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "xxxx")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("로그아웃")
    @Test
    public void logout() throws Exception {
        mvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}
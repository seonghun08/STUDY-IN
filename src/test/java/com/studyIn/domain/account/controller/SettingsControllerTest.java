package com.studyIn.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.account.value.Gender;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

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

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 폼")
    @Test
    public void updateProfileForm() throws Exception {
        mvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 오류")
    @Test
    public void updateProfile_fail() throws Exception {
        String bio = "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요" +
                "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요";
        String link = "www.link.com";
        String occupation = "Back-end Dev";

        mvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .param("link", link)
                        .param("occupation", occupation)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/profile"))
                .andExpect(model().attributeExists("accountInfo"));

        Account account = accountRepository.findByUsername("user").orElseThrow();
        assertThat(bio).isNotEqualTo(account.getProfile().getBio());
        assertThat(link).isNotEqualTo(account.getProfile().getLink());
        assertThat(occupation).isNotEqualTo(account.getProfile().getOccupation());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 정상")
    @Test
    public void updateProfile_success() throws Exception {
        String nickname = "flash";
        String bio = "안녕하세요.";
        String link = "www.url.com";
        String occupation = "Back-end Dev";

        mvc.perform(post("/settings/profile")
                        .param("nickname", nickname)
                        .param("bio", bio)
                        .param("link", link)
                        .param("occupation", occupation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user").orElseThrow();
        assertThat(nickname).isEqualTo(account.getProfile().getNickname());
        assertThat(bio).isEqualTo(account.getProfile().getBio());
        assertThat(link).isEqualTo(account.getProfile().getLink());
        assertThat(occupation).isEqualTo(account.getProfile().getOccupation());
    }
}
package com.studyIn.domain.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.entity.QAccount;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccontSettingsService;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.entity.QStudy;
import com.studyIn.domain.study.entity.QStudyManager;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.service.StudyService;
import com.studyIn.domain.tag.TagRepository;
import org.assertj.core.api.Assertions;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.study.entity.QStudy.study;
import static com.studyIn.domain.study.entity.QStudyManager.studyManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @PersistenceContext EntityManager em;
    @Autowired MockMvc mvc;
    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired AccountService accountService;
    @Autowired StudyService studyService;
    @Autowired AccontSettingsService accontSettingsService;
    @Autowired AccountRepository accountRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @BeforeEach
    void beforeEach() {
        accountFactory.createAccount("user");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 폼")
    @Test
    public void newStudyForm() throws Exception {
        mvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 실패 - 이미 있는 경로로 만들 경우")
    @Test
    public void newStudy_fail1() throws Exception {
        Account account = accountFactory.createAccount("manager");
        studyFactory.createStudy("path", account);

        mvc.perform(post("/new-study")
                        .param("path", "path")
                        .param("title", "title")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full fullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountInfo"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 실패 - 입력 값 오류")
    @Test
    public void newStudy_fail2() throws Exception {
        mvc.perform(post("/new-study")
                        .param("path", "    blank   ")
                        .param("title", "")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full fullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountInfo"));
        assertThat(studyRepository.findByPath("    blank   ")).isEmpty();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 성공")
    @Test
    public void newStudy_success() throws Exception {
        mvc.perform(post("/new-study")
                        .param("path", "new-path")
                        .param("title", "new-title")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full fullDescription")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/new-path"));

        Account account = accountRepository.findByUsername("user").orElseThrow();
        Study study = studyRepository.findWithManagersByPath("new-path").orElseThrow();

        boolean check = study.getManagers().stream()
                .map(m -> m.getAccount().getUsername())
                .collect(Collectors.toList())
                .contains(account.getUsername());
        assertThat(check).isTrue();

        assertThat(study).isNotNull();
        assertThat(study.getPath()).isEqualTo("new-path");
        assertThat(study.getTitle()).isEqualTo("new-title");
        assertThat(study.getShortDescription()).isEqualTo("short description");
        assertThat(study.getFullDescription()).isEqualTo("full fullDescription");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 조회 성공")
    @Test
    public void viewStudy() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", account);

        mvc.perform(get("/study/" + study.getPath()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect((model().attributeExists("study")));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 회원 조회 성공")
    @Test
    void viewStudyMembers() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(get("/study/" + study.getPath() + "/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"));
    }
}
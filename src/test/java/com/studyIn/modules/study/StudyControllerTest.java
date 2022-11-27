package com.studyIn.modules.study;

import com.studyIn.infra.AbstractContainerBaseTest;
import com.studyIn.infra.MockMvcTest;
import com.studyIn.modules.account.*;
import com.studyIn.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudyControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mvc;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("nickname");
        signUpForm.setUsername("user");
        signUpForm.setEmail("user@email.com");
        signUpForm.setGenderType(GenderType.none);
        signUpForm.setBirthday("1997-08-30");
        signUpForm.setPassword("123123123");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 폼")
    @Test
    public void newStudyForm() throws Exception {
        mvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 등록")
    @Test
    public void newStudySubmit_success() throws Exception {
        mvc.perform(post("/new-study")
                        .param("path", "new-path")
                        .param("title", "new-title")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full fullDescription")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/new-path"));

        Account account = accountService.findByUsername("user");
        Study study = studyService.findPath("new-path");

        assertNotNull(study);
        assertTrue(study.getManagers().contains(account));
        assertEquals(study.getPath(), "new-path");
        assertEquals(study.getTitle(), "new-title");
        assertEquals(study.getShortDescription(), "short description");
        assertEquals(study.getFullDescription(), "full fullDescription");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 실패")
    @Test
    public void newStudySubmit_fail() throws Exception {
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
                .andExpect(model().attributeExists("account"));

        mvc.perform(post("/new-study")
                        .param("path", "    blank   ")
                        .param("title", "")
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full fullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"));

        assertNull(studyRepository.findByPath("    blank   "));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 조회")
    @Test
    public void viewStudy() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", account);

        mvc.perform(get("/study/" + study.getPath()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect((model().attributeExists("study")));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 회원 조회")
    @Test
    void viewStudyMembers() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Account member = accountRepository.findByUsername("user");

        Study study = studyFactory.createStudy("path", manager);
        studyService.addMember(member, study);

        mvc.perform(get("/study/" + study.getPath() + "/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

        assertTrue(study.getMembers().contains(member));
        assertTrue(study.getManagers().contains(manager));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 가입")
    @Test
    void joinStudy() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(post("/study/" + study.getPath() + "/join")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account member = accountRepository.findByUsername("user");
        assertTrue(study.getMembers().contains(member));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 탈퇴")
    @Test
    void leaveStudy() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Account member = accountRepository.findByUsername("user");

        Study study = studyFactory.createStudy("study", manager);
        studyService.addMember(member, study);
        assertTrue(study.getMembers().contains(member));

        mvc.perform(post("/study/" + study.getPath() + "/leave")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(member));
    }
}
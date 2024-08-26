package com.studyIn.domain.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationForm;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.dto.form.TitleForm;
import com.studyIn.domain.study.entity.*;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.tag.QTag;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagForm;
import com.studyIn.domain.tag.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudySettingsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired StudyRepository studyRepository;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired ObjectMapper objectMapper;

    private Location testLocation = Location.createLocation("Seoul", "서울특별시", "none");

    @BeforeEach
    void beforeEach() {
        accountFactory.createAccount("user");
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 소개 설정 폼 - GET 실패 (접근 권한 없음)")
    @Test
    void descriptionSettingsForm_NotPermissions() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }
    
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 소개 설정 폼 - GET 성공")
    @Test
    void descriptionSettingsForm_permissions() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("descriptionForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 소개 설정 - POST 실패 (접근 권한 없음)")
    @Test
    void descriptionSettingsUpdate_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(post("/study/" + study.getPath() + "/settings/description")
                        .param("shortDescription", "update_short")
                        .param("fullDescription", "update_full")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 소개 설정 - POST 성공")
    @Test
    void descriptionSettingsUpdate_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/description")
                        .param("shortDescription", "update_short")
                        .param("fullDescription", "update_full")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/description"))
                .andExpect(flash().attributeExists("message"));

        assertEquals("update_short", study.getShortDescription());
        assertEquals("update_full", study.getFullDescription());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 설정 - GET 실패 (접근 권한 없음)")
    @Test
    void bannerSettings_NotPermissions() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(get("/study/" + study.getPath() + "/settings/banner"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 설정 - GET 성공")
    @Test
    void bannerSettings_permissions() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(get("/study/" + study.getPath() + "/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/banner"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 활성화 - POST 실패 (접근 권한 없음)")
    @Test
    void enableBanner_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(post("/study/" + study.getPath() + "/settings/banner/enable")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(study.isUseBanner());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 활성화 - POST 성공")
    @Test
    void enableBanner_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/banner/enable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/banner"));

        assertTrue(study.isUseBanner());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 비활성화 - POST 실패 (접근 권한 없음)")
    @Test
    void disableBanner_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);
        study.updateUseBanner(true);

        mvc.perform(post("/study/" + study.getPath() + "/settings/banner/disable")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(study.isUseBanner());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 배너 비활성화 - POST 성공")
    @Test
    void disableBanner_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);
        study.updateUseBanner(true);

        mvc.perform(post("/study/" + study.getPath() + "/settings/banner/disable")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/banner"));

        assertFalse(study.isUseBanner());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태그 설정 - GET 실패 (접근 권한 없음)")
    @Test
    void TagSettingsForm_NotPermissions() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("study", manager);

        mvc.perform(get("/study/" + study.getPath() + "/settings/tags"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태그 설정 - GET 성공")
    @Test
    void TagSettingsForm_permissions() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("study", user);

        mvc.perform(get("/study/" + study.getPath() + "/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/tags"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태그 추가 - POST 실패 (접근 권한 없음)")
    @Test
    void addTags_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(post("/study/" + study.getPath() + "/settings/tags/add")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태크 추가 - POST 성공")
    @Test
    void addTags_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring");

        mvc.perform(post("/study/" + study.getPath() + "/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle("spring")
                .orElseThrow(RuntimeException::new);
        StudyTag studyTag = jpaQueryFactory
                .selectFrom(QStudyTag.studyTag)
                .join(QStudyTag.studyTag.study, QStudy.study)
                .join(QStudyTag.studyTag.tag, QTag.tag)
                .where(QStudy.study.id.eq(study.getId()))
                .fetchOne();

        assertThat(studyTag.getTag().getId()).isEqualTo(tag.getId());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태크 제거 - POST 실패 (접근 권한 없음)")
    @Test
    void removeTags_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        Tag tag = Tag.createTag("spring");
        tagRepository.save(tag);
        StudyTag studyTag = StudyTag.createStudyTag(tag);
        study.addStudyTag(studyTag);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring");

        mvc.perform(post("/study/" + study.getPath() + "/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주제 태크 제거 - POST 성공")
    @Test
    void removeTags_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        Tag tag = Tag.createTag("spring");
        tagRepository.save(tag);
        StudyTag studyTag = StudyTag.createStudyTag(tag);
        study.addStudyTag(studyTag);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring");

        mvc.perform(post("/study/" + study.getPath() + "/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 지역 태그 설정 - GET 실패 (접근 권한 없음)")
    @Test
    void locationsSettingsForm_NotPermissions() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(get("/study/" + study.getPath() + "/settings/locations"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 지역 설정 - GET 성공")
    @Test
    void locationsSettingsForm_permissions() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(get("/study/" + study.getPath() + "/settings/locations"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/locations"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 지역 추가 - POST 실패 (접근 권한 없음)")
    @Test
    void addLocations_fail() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", account);

        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        mvc.perform(post("/study/" + study.getPath() + "/settings/locations/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 지역 제거 - POST 성공")
    @Test
    void removeLocations_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        Location location = locationRepository.findByCityAndProvince(testLocation.getCity(), testLocation.getProvince())
                .orElseThrow(RuntimeException::new);
        StudyLocation studyLocation = StudyLocation.createStudyLocation(location);
        study.addStudyLocation(studyLocation);

        mvc.perform(post("/study/" + study.getPath() + "/settings/locations/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 설정 폼 - GET 실패 (접근 권한 없음)")
    @Test
    void studySettingsForm_NotPermissions() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("path", manager);

        mvc.perform(get("/study/" + study.getPath() + "/settings/study"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 설정 폼 - GET 성공")
    @Test
    void studySettingsForm_permissions() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(get("/study/" + study.getPath() + "/settings/study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/study"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("pathForm"))
                .andExpect(model().attributeExists("titleForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 공개 - POST 실패 (이미 스터디를 공개한 상황일 경우)")
    @Test
    void publishStudy_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);
        study.publish();

        assertThrows(NestedServletException.class, () ->
                mvc.perform(post("/study/" + study.getPath() + "/settings/study/publish")
                        .with(csrf())));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 종료 - POST 실패 (아직 스터디 공개를 하지 않았으나, POST 요청이 들어올 경우")
    @Test
    void closeStudy_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        assertThrows(NestedServletException.class, () ->
                mvc.perform(post("/study/" + study.getPath() + "/settings/study/close")
                        .with(csrf())));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 종료 - POST 성공")
    @Test
    void closeStudy_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);
        study.publish();

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/close")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/study"))
                .andExpect(flash().attributeExists("message"));

        assertTrue(study.isClosed());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 팀원 모집 - POST 실패 (팀원 모집을 30분 이내 설정한 경우 변경 불가능)")
    @Test
    void startRecruit_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);
        study.publish();
        study.stopRecruit();

        mvc.perform(post("/study/" + study.getPath() + "/settings/recruit/start")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/study"))
                .andExpect(flash().attributeExists("message"));

        Study findStudy = studyRepository.findByPath("path")
                .orElseThrow(RuntimeException::new);
        assertThat(findStudy.isRecruiting()).isFalse();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주소 변경 - POST 성공")
    @Test
    void updateStudyPath_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/path")
                        .param("path", "update-path")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/study"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(study.getPath(), "update-path");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 주소 변경 - 실패 (타 스터디 모임의 주소와 동일하면 error 발생)")
    @Test
    void updateStudyPath_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        studyFactory.createStudy("test-path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/path")
                        .param("path", "test-path")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/study"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("pathForm"))
                .andExpect(model().attributeExists("titleForm"))
                .andExpect(model().attributeExists("message"));

        assertNotEquals("test-path", study.getPath());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 이름 변경 - POST 성공")
    @Test
    void updateStudyTitle_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/title")
                        .param("title", "update-title")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/study"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(study.getTitle(), "update-title");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 이름 변경 - POST 실패 (타 스터디 모임의 이름과 동일할 경우 error 발생")
    @Test
    void updateStudyTitle_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        Study testStudy = studyFactory.createStudy("test-path", user);

        TitleForm titleForm = new TitleForm();
        titleForm.setTitle("test-title");
        testStudy.updateTitle(titleForm);

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/title")
                        .param("path", "test-title")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/study"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("pathForm"))
                .andExpect(model().attributeExists("titleForm"))
                .andExpect(model().attributeExists("message"));

        assertNotEquals(study.getTitle(), "update-title");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 삭제 - POST 성공")
    @Test
    void removeStudy_success() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);

        mvc.perform(post("/study/" + study.getPath() + "/settings/study/remove")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 삭제 - POST 실패 (공개된 스터디는 삭제 불가능)")
    @Test
    void removeStudy_fail() throws Exception {
        Account user = accountRepository.findByUsername("user")
                .orElseThrow(RuntimeException::new);
        Study study = studyFactory.createStudy("path", user);
        study.publish();

        assertThrows(NestedServletException.class, () ->
                mvc.perform(post("/study/" + study.getPath() + "/settings/study/remove")
                        .with(csrf()))
        );
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 설정 - POST 실패 (접근 권한 없음)")
    @Test
    void studySettings_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("study", manager);

        // POST - publish
        mvc.perform(post("/study/" + study.getPath() + "/settings/study/publish"))
                .andExpect(status().isForbidden());

        // POST - close
        mvc.perform(post("/study/" + study.getPath() + "/settings/study/close"))
                .andExpect(status().isForbidden());

        // POST - recruit start
        mvc.perform(post("/study/" + study.getPath() + "/settings/recruit/start"))
                .andExpect(status().isForbidden());

        // POST - recruit stop
        mvc.perform(post("/study/" + study.getPath() + "/settings/recruit/stop"))
                .andExpect(status().isForbidden());

        // POST - path
        mvc.perform(post("/study/" + study.getPath() + "/settings/study/path"))
                .andExpect(status().isForbidden());

        // POST - title
        mvc.perform(post("/study/" + study.getPath() + "/settings/study/title"))
                .andExpect(status().isForbidden());

        // POST - remove
        mvc.perform(post("/study/" + study.getPath() + "/settings/study/remove"))
                .andExpect(status().isForbidden());
    }
}
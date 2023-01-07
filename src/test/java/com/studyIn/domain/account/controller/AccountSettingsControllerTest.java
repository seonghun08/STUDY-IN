package com.studyIn.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.service.AccontSettingsService;
import com.studyIn.domain.account.service.AccountService;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationForm;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.location.QLocation;
import com.studyIn.domain.tag.QTag;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagForm;
import com.studyIn.domain.tag.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountSettingsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired AccontSettingsService accontSettingsService;
    @Autowired AccountRepository accountRepository;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;

    private Location testLocation = Location.createLocation("Seoul", "서울특별시", "none");

    @BeforeEach
    void beforeEach() {
        accountFactory.createAccount("user");
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(authenticated().withUsername("user"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 오류")
    @Test
    void updateProfile_fail() throws Exception {
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
    @DisplayName("프로필 수정 성공")
    @Test
    void updateProfile_success() throws Exception {
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

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 오류 - 현재 패스워드가 일치하지 않을 경우")
    @Test
    void updatePassword_fail1() throws Exception {
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "xxxxxxxx")
                        .param("newPassword", "1234567890")
                        .param("confirmPassword", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("accountInfo"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 오류 - 새 패스워드와 확인 패스워드가 서로 일치하지 않을 경우")
    @Test
    void updatePassword_fail2() throws Exception {
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", "xxxxxxxx")
                        .param("confirmPassword", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("accountInfo"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 오류 - 새 패스워드의 최소 범위에 못 미칠 경우")
    @Test
    void updatePassword_fail3() throws Exception {
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", "xxxx")
                        .param("confirmPassword", "xxxx")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("accountInfo"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 성공")
    @Test
    void updatePassword_success() throws Exception {
        String newPassword = "1234567890";

        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", newPassword)
                        .param("confirmPassword", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user").orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, account.getPassword())).isTrue();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 수정 폼")
    @Test
    void updateNotificationsForm() throws Exception {
        mvc.perform(get("/settings/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("notificationsSettingForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 수정 성공")
    @Test
    void updateNotifications() throws Exception {
        mvc.perform(post("/settings/notifications")
                        .param("studyCreatedByEmail", "true")
                        .param("studyEnrollmentResultByEmail", "true")
                        .param("studyUpdatedByEmail", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user").orElseThrow();
        assertThat(account.getAuthentication().getNotificationsSetting().isStudyCreatedByEmail()).isTrue();
        assertThat(account.getAuthentication().getNotificationsSetting().isStudyUpdatedByEmail()).isTrue();
        assertThat(account.getAuthentication().getNotificationsSetting().isStudyEnrollmentResultByEmail()).isTrue();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("주제태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mvc.perform(get("/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("주제태그 수정 성공 - 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring boot");

        mvc.perform(post("/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle(tagForm.getTitle()).orElseThrow();
        AccountTag accountTag = jpaQueryFactory
                .selectFrom(QAccountTag.accountTag)
                .join(QAccountTag.accountTag.account, QAccount.account).fetchJoin()
                .join(QAccountTag.accountTag.tag, QTag.tag).fetchJoin()
                .where(QAccount.account.username.eq("user"))
                .fetchOne();

        assertThat(accountTag.getTag().getTitle()).isEqualTo(tag.getTitle());
        assertThat(accountTag.getAccount().getUsername()).isEqualTo("user");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 수정 성공 - 삭제")
    @Test
    public void removeTag() throws Exception {
        Tag saveTag = tagRepository.save(Tag.createTag("spring boot"));
        Account saveAccount = accountRepository.findByUsername("user").orElseThrow();
        AccountInfo accountInfo = new AccountInfo(saveAccount);
        accontSettingsService.addTag(saveTag, accountInfo);

        TagForm tagForm = new TagForm();
        tagForm.setTitle("spring boot");

        mvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        AccountTag accountTag = jpaQueryFactory
                .selectFrom(QAccountTag.accountTag)
                .join(QAccountTag.accountTag.account, QAccount.account).fetchJoin()
                .join(QAccountTag.accountTag.tag, QTag.tag).fetchJoin()
                .where(QAccount.account.username.eq("user"))
                .fetchOne();

        assertThat(tagRepository.findByTitle(tagForm.getTitle()).isEmpty()).isFalse();
        assertThat(accountTag == null).isTrue();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역태그 수정 폼")
    @Test
    void updateLocationsForm() throws Exception {
        mvc.perform(get("/settings/locations"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역태그 수정 성공 - 추가")
    @Test
    void addLocation() throws Exception {
        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        mvc.perform(post("/settings/locations/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Location location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName()).orElseThrow();
        AccountLocation accountLocation = jpaQueryFactory
                .selectFrom(QAccountLocation.accountLocation)
                .join(QAccountLocation.accountLocation.account, QAccount.account).fetchJoin()
                .join(QAccountLocation.accountLocation.location, QLocation.location).fetchJoin()
                .where(QAccount.account.username.eq("user"))
                .fetchOne();

        assertThat(accountLocation.getLocation().toString()).isEqualTo(location.toString());
        assertThat(accountLocation.getAccount().getUsername()).isEqualTo("user");
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역태그 수정 성공 - 삭제")
    @Test
    public void removeLocation() throws Exception {
        Location location = locationRepository.findByCityAndProvince(testLocation.getCity(), testLocation.getProvince()).orElseThrow();
        Account saveAccount = accountRepository.findByUsername("user").orElseThrow();
        AccountInfo accountInfo = new AccountInfo(saveAccount);
        accontSettingsService.addLocation(location, accountInfo);

        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        mvc.perform(post("/settings/locations/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        AccountLocation accountLocation = jpaQueryFactory
                .selectFrom(QAccountLocation.accountLocation)
                .join(QAccountLocation.accountLocation.account, QAccount.account).fetchJoin()
                .join(QAccountLocation.accountLocation.location, QLocation.location).fetchJoin()
                .where(QAccount.account.username.eq("user"))
                .fetchOne();

        assertThat(locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName()).isEmpty()).isFalse();
        assertThat(accountLocation == null).isTrue();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 폼")
    @Test
    public void updateAccountForm() throws Exception {
        mvc.perform(get("/settings/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/account"))
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("usernameForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 실패 - 입력 값이 올바르지 않은 경우, 대문자 불가능")
    @Test
    public void updateAccount_fail1() throws Exception {
        mvc.perform(post("/settings/account")
                        .param("username", "USER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("usernameForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 실패 - 현재 아이디와 새 아이디가 서로 일치하는 경우")
    @Test
    public void updateAccount_fail2() throws Exception {
        mvc.perform(post("/settings/account")
                        .param("username", "user")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("usernameForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 실패 - 이미 존재하는 아이디를 입력하는 경우")
    @Test
    public void updateAccount_fail3() throws Exception {
        accountFactory.createAccount("old-user");

        mvc.perform(post("/settings/account")
                        .param("username", "old-user")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountInfo"))
                .andExpect(model().attributeExists("usernameForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 성공")
    @Test
    public void updateAccount_success() throws Exception {
        String username = "new-user";

        mvc.perform(post("/settings/account")
                        .param("username", username)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByEmail("user@email.com").orElseThrow();
        assertEquals(account.getUsername(), username);
    }
}
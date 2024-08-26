package com.studyIn.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.infra.AbstractContainerBaseTest;
import com.studyIn.infra.MockMvcTest;
import com.studyIn.modules.account.form.SignUpForm;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.location.LocationRepository;
import com.studyIn.modules.location.LocationService;
import com.studyIn.modules.location.form.LocationForm;
import com.studyIn.modules.tag.Tag;
import com.studyIn.modules.tag.TagRepository;
import com.studyIn.modules.tag.form.TagForm;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired TagRepository tagRepository;
    @Autowired LocationService locationService;
    @Autowired LocationRepository locationRepository;

    private Location testLocation = Location.builder().city("Seoul").localNameOfCity("서울특별시").province("none").build();

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
    @DisplayName("프로필 수정 폼")
    @Test
    public void updateProfileForm() throws Exception {
        mvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 정상")
    @Test
    public void updateProfile_success() throws Exception {
        String nickname = "flash";
        String bio = "안녕하세요.";
        String url = "www.url.com";
        String occupation = "Back-end Dev";

        mvc.perform(post("/settings/profile")
                        .param("nickname", nickname)
                        .param("bio", bio)
                        .param("url", url)
                        .param("occupation", occupation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user");
        Assertions.assertEquals(nickname, account.getNickname());
        Assertions.assertEquals(bio, account.getBio());
        Assertions.assertEquals(url, account.getUrl());
        Assertions.assertEquals(occupation, account.getOccupation());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 오류")
    @Test
    public void updateProfile_fail() throws Exception {
        String bio = "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요" +
                     "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요";
        String url = "www.url.com";
        String occupation = "Back-end Dev";

        mvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .param("url", url)
                        .param("occupation", occupation)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"));

        Account account = accountRepository.findByUsername("user");
        Assertions.assertNotEquals(bio, account.getBio());
        Assertions.assertNotEquals(url, account.getBio());
        Assertions.assertNotEquals(occupation, account.getBio());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    @Test
    public void updatePasswordForm() throws Exception {
        mvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력 값 정상")
    @Test
    public void updatePassword_success() throws Exception {
        String newPassword = "1234567890";

        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", newPassword)
                        .param("confirmPassword", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user");
        assertTrue(passwordEncoder.matches(newPassword, account.getPassword()));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력 값 오류")
    @Test
    public void updatePassword_fail() throws Exception {
        // 현재 패스워드가 일치하지 않을 경우
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "xxxxxxxx")
                        .param("newPassword", "1234567890")
                        .param("confirmPassword", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

        // 새 패스워드와 확인 패스워드가 서로 일치하지 않을 경우
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", "xxxxxxxx")
                        .param("confirmPassword", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

        // 새 패스워드의 최소 범위에 못 미칠 경우
        mvc.perform(post("/settings/password")
                        .param("currentPassword", "123123123")
                        .param("newPassword", "xxxx")
                        .param("confirmPassword", "xxxx")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 수정 폼")
    @Test
    public void updateNotificationsForm() throws Exception {
        mvc.perform(get("/settings/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationsForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 수정하기 - 변경값 정상")
    @Test
    public void updateNotifications() throws Exception {
        mvc.perform(post("/settings/notifications")
                        .param("studyCreatedByEmail", "true")
                        .param("studyEnrollmentResultByEmail", "true")
                        .param("studyUpdatedByEmail", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUsername("user");
        assertTrue(account.isStudyCreatedByEmail());
        assertTrue(account.isStudyEnrollmentResultByEmail());
        assertTrue(account.isStudyUpdatedByEmail());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 수정 폼")
    @Test
    public void updateTagsForm() throws Exception {
        mvc.perform(get("/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 수정 - 추가")
    @Test
    public void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("spring boot");

        mvc.perform(post("/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag Tag = tagRepository.findByTitle("spring boot").get();
        Account account = accountRepository.findByUsername("user");
        assertEquals(Tag.getTitle(), tagForm.getTagTitle());
        assertTrue(account.getTags().contains(Tag));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 수정 - 삭제")
    @Test
    public void removeTag() throws Exception {
        Tag savedTag = tagRepository.save(Tag.builder().title("spring boot").build());
        Account account = accountRepository.findByUsername("user");
        accountService.addTag(savedTag, account);
        assertTrue(account.getTags().contains(savedTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("spring boot");

        mvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(savedTag));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 수정 폼")
    @Test
    public void updateLocationsForm() throws Exception {
        mvc.perform(get("/settings/locations"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/locations"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attributeExists("savedList"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 수정 - 추가")
    @Test
    public void addLocation() throws Exception {
        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        mvc.perform(post("/settings/locations/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Location location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());
        Account account = accountRepository.findByUsername("user");
        assertEquals(location.toString(), locationForm.getLocationName());
        assertTrue(account.getLocations().contains(location));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 수정 - 삭제")
    @Test
    public void removeLocation() throws Exception {
        LocationForm locationForm = new LocationForm();
        locationForm.setLocationName(testLocation.toString());

        Account account = accountRepository.findByUsername("user");
        Location location = locationService.findLocation(locationForm);
        accountService.addLocation(location, account);
        assertTrue(account.getLocations().contains(location));

        mvc.perform(post("/settings/locations/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getLocations().contains(location));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 폼")
    @Test
    public void updateAccountForm() throws Exception {
        mvc.perform(get("/settings/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("usernameForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 - 입력 값 정상")
    @Test
    public void updateAccount_success() throws Exception {
        String username = "updateUser";

        mvc.perform(post("/settings/account")
                        .param("username", username)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByEmail("user@email.com");
        assertEquals(account.getUsername(), username);
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 수정 - 입력 값 오류")
    @Test
    public void updateAccount_fail() throws Exception {
        // 현재 아이디와 새 아이디가 서로 일치하는 경우
        mvc.perform(post("/settings/account")
                        .param("username", "user")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("usernameForm"));

        // 이미 존재하는 아이디를 입력하는 경우
        Account account = new Account();
        account.setUsername("oldUser");
        accountRepository.save(account);

        mvc.perform(post("/settings/account")
                        .param("username", "oldUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("usernameForm"));
    }
}
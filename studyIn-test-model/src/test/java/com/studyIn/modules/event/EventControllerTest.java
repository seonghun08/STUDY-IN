package com.studyIn.modules.event;

import com.studyIn.infra.AbstractContainerBaseTest;
import com.studyIn.infra.MockMvcTest;
import com.studyIn.modules.account.*;
import com.studyIn.modules.account.form.SignUpForm;
import com.studyIn.modules.study.Study;
import com.studyIn.modules.study.StudyFactory;
import com.studyIn.modules.study.StudyRepository;
import com.studyIn.modules.study.StudyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mvc;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired EventService eventService;
    @Autowired EventRepository eventRepository;
    @Autowired EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("nickname");
        signUpForm.setUsername("user");
        signUpForm.setEmail("user@email.com");
        signUpForm.setGenderType(GenderType.none);
        signUpForm.setBirthday("1997-08-30");
        signUpForm.setPassword("123123123");
        Account account = accountService.processNewAccount(signUpForm);

        studyFactory.createStudy("path", account);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        studyRepository.deleteAll();
    }

    protected Event createEvent(String title, EventType eventType,
                                int limit, Study study, Account account) {
        Event event = new Event();
        event.setTitle(title);
        event.setEventType(eventType);
        event.setDescription("<p>description</p>");
        event.setLimitOfEnrollments(limit);
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(7));
        return eventService.createEvent(study, account, event);
    }

    protected Event oldEvent() {
        Account account = accountRepository.findByUsername("user");
        Study study = studyRepository.findByPath("path");

        Event event = new Event();
        event.setTitle("old_title");
        event.setDescription("<p>old_description</p>");
        event.setEndEnrollmentDateTime(LocalDateTime.now().minusDays(10));
        event.setStartDateTime(LocalDateTime.now().minusDays(5));
        event.setEndDateTime(LocalDateTime.now().minusDays(1));
        return eventService.createEvent(study, account, event);
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 폼 - GET 성공")
    @Test
    void newEventForm_permissions() throws Exception {
        mvc.perform(get("/study/path/new-event"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 폼 - GET 실패 (권한 없음)")
    @Test
    void newEventForm_NotPermissions() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);

        mvc.perform(get("/study/" + study.getPath() + "/new-event"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 - POST 성공")
    @Test
    void newEventSubmit_success() throws Exception {
        mvc.perform(post("/study/path/new-event")
                        .param("title", "event_title")
                        .param("description", "event_description")
                        .param("eventType", String.valueOf(EventType.MANAGER_CHECK))
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Event event = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().equals("event_title"))
                .findAny().get();

        assertEquals(event.getEventType(), EventType.MANAGER_CHECK);
        assertEquals(event.getDescription(), "event_description");
        mvc.perform(get("/study/path/events/" + event.getId()))
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 - POST 실패 (등록 마감 일시가 현재보다 늦을 때)")
    @Test
    void newEventSubmit_fail_endEnrollmentDateTime() throws Exception {
        mvc.perform(post("/study/path/new-event")
                        .param("title", "event_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().minusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 - POST 실패 (모임 시작 일시가 현재보다 늦을 때)")
    @Test
    void newEventSubmit_fail_startDateTime() throws Exception {
        mvc.perform(post("/study/path/new-event")
                        .param("title", "event_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().minusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 - POST 실패 (모임 종료 일시가 현재보다 늦을 때)")
    @Test
    void newEventSubmit_fail_endDateTime() throws Exception {
        mvc.perform(post("/study/path/new-event")
                        .param("title", "event_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().minusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 만들기 - POST 실패 (권한 없음)")
    @Test
    void newEventSubmit_NotPermissions() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);

        mvc.perform(post("/study/" + study.getPath() + "/new-event")
                        .param("title", "event_title")
                        .param("description", "event_description")
                        .param("eventType", String.valueOf(EventType.MANAGER_CHECK))
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        long count = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().equals("event_title")).count();
        assertEquals(0, count);
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 조회 - GET 성공")
    @Test
    void viewEvent() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(get("/study/path/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));

        Event findEvent = eventRepository.findById(event.getId()).get();
        assertEquals(findEvent, event);
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 목록 보기 - GET 성공")
    @Test
    void viewStudyEvents() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");

        Event newEvent = createEvent("new_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);
        Event oldEvent = oldEvent();

        mvc.perform(get("/study/path/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/events"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"));

        EventList eventList = eventService.findEventsOrderByNewEventAndOldEvent(study);
        assertTrue(eventList.getNewEvents().contains(newEvent));
        assertTrue(eventList.getOldEvents().contains(oldEvent));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 폼 - GET 성공")
    @Test
    void updateEventForm() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(get("/study/path/events/" + event.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/update-form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("eventForm"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 폼 - GET 실패 (관리자가 아닌 일반 회원일 경우)")
    @Test
    void updateEventForm_NotPermissions() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(get("/study/" + study.getPath() + "/events/" + event.getId() + "/edit"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 성공")
    @Test
    void updateEventSubmit_success() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(post("/study/path/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("limitOfEnrollments", String.valueOf(10))
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Event findEvent = eventRepository.findById(event.getId()).get();
        assertEquals(findEvent.getTitle(), "update_title");
        assertEquals(10, findEvent.getLimitOfEnrollments());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 실패 (모집 인원이 3명 이상인데 2명으로 수정할 경우)")
    @Test
    void updateEventSubmit_fail_limitOfEnrollments() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        Account member1 = accountFactory.createAccount("member1");
        Account member2 = accountFactory.createAccount("member2");

        studyService.addMember(member1, study);
        studyService.addMember(member2, study);
        eventService.newEnrollment(study, event, member1);
        eventService.newEnrollment(study, event, member2);

        mvc.perform(post("/study/path/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("limitOfEnrollments", String.valueOf(2))
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/update-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 실패 (등록 마감 일시를 현재보다 늦게 수정할 경우)")
    @Test
    void updateEventSubmit_fail_endEnrollmentDateTime() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(post("/study/path/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().minusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/update-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 실패 (모임 시작 일시를 현재보다 늦게 수정할 경우)")
    @Test
    void updateEventSubmit_fail_startDateTime() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(post("/study/path/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().minusDays(10)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/update-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 실패 (모임 종료 일시를 현재보다 늦게 수정할 경우)")
    @Test
    void updateEventSubmit_fail_endDateTime() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(post("/study/path/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(10)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().minusDays(7)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/update-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("event"));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 수정 - POST 실패 (권한 없음)")
    @Test
    void updateEventSubmit_fail_NotPermissions() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/edit")
                        .param("title", "update_title")
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(10)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 삭제 - POST 성공")
    @Test
    void cancelEvent_success() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(delete("/study/path/events/" + event.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events"));

        assertFalse(eventRepository.findById(event.getId()).isPresent());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 삭제 - POST 실패 (권한 없음)")
    @Test
    void cancelEvent_fail() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        mvc.perform(delete("/study/" + study.getPath() + "/events/" + event.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(eventRepository.findById(event.getId()).isPresent());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("선착순 모임 신청 - POST 성공 (선착순 모임은 제한 인원까지 자동 수락이 된다.)")
    @Test
    void newEnrollment_FIRST_COME_FIRST_SERVED_accepted() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 2, study, account);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "참가 신청이 완료되었습니다."));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertNotNull(enrollment);
        assertTrue(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("선착순 모임 신청 - POST 성공 (선착순 모임은 제한 인원을 넘으면 대기중으로 된다.)")
    @Test
    void newEnrollment_FIRST_COME_FIRST_SERVED_notAccepted() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 2, study, account);

        Account member1 = accountFactory.createAccount("member1");
        Account member2 = accountFactory.createAccount("member2");

        studyService.addMember(member1, study);
        studyService.addMember(member2, study);
        eventService.newEnrollment(study, event, member1);
        eventService.newEnrollment(study, event, member2);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "참가 신청이 완료되었습니다."));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertNotNull(enrollment);
        assertFalse(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 확인 모임 신청 - POST 성공 (대기중)")
    @Test
    void newEnrollment_to_CONFIMATIVE_notAccepted() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account user = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 2, study, user);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, user);
        assertFalse(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 신청 - 실패 (스터디 회원이 아닐 경우)")
    @Test
    void newEnrollment_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", manager);
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, manager);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "스터디 가입을 먼저 해주세요."));

        Account user = accountRepository.findByUsername("user");
        assertNull(enrollmentRepository.findByEventAndAccount(event, user));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 취소 - POST 성공")
    @Test
    void cancelEnrollment_success() throws Exception {
        Study study = studyService.findPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);
        assertTrue(eventService.newEnrollment(study, event, account));

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disEnroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "참가 신청이 취소되었습니다."));

        assertNull(enrollmentRepository.findByEventAndAccount(event, account));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("선착순 모임 취소 - POST 성공 (다음 대기자가 있을 경우 자동으로 다음 대기자가 자동 수락된다.)")
    @Test
    void cancelEnrollment_FIRST_COME_FIRST_SERVED_not_accepted() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account manager = accountRepository.findByUsername("user");

        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 2, study, manager);
        eventService.newEnrollment(study, event, manager);

        Account member1 = accountFactory.createAccount("member1");
        Account member2 = accountFactory.createAccount("member2");

        studyService.addMember(member1, study);
        studyService.addMember(member2, study);
        eventService.newEnrollment(study, event, member1);
        eventService.newEnrollment(study, event, member2);

        Enrollment enrollment_member2 = enrollmentRepository.findByEventAndAccount(event, member2);
        assertFalse(enrollment_member2.isAccepted());

        mvc.perform(post("/study/path/events/" + event.getId() + "/disEnroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "참가 신청이 취소되었습니다."));

        assertTrue(enrollment_member2.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("선착순 모임 취소 - POST 성공 (대기자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 수락자를 그대로 유지하고 새로운 수락자는 없다.)")
    @Test
    void not_accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Study study = studyRepository.findByPath("path");
        Account manager = accountRepository.findByUsername("user");

        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 2, study, manager);

        Account member1 = accountFactory.createAccount("member1");
        Account member2 = accountFactory.createAccount("member2");

        studyService.addMember(member1, study);
        studyService.addMember(member2, study);
        eventService.newEnrollment(study, event, member1);
        eventService.newEnrollment(study, event, member2);
        eventService.newEnrollment(study, event, manager);

        Enrollment enrollment_member1 = enrollmentRepository.findByEventAndAccount(event, member1);
        Enrollment enrollment_member2 = enrollmentRepository.findByEventAndAccount(event, member2);
        Enrollment enrollment_manager = enrollmentRepository.findByEventAndAccount(event, manager);

        assertTrue(enrollment_member1.isAccepted());
        assertTrue(enrollment_member2.isAccepted());
        assertFalse(enrollment_manager.isAccepted());

        mvc.perform(post("/study/path/events/" + event.getId() + "/disEnroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(flash().attribute("message", "참가 신청이 취소되었습니다."));

        assertTrue(enrollment_member1.isAccepted());
        assertTrue(enrollment_member2.isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, manager));
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("모임 취소 - POST 실패 (신청을 안한 상태에서 취소를 할 경우)")
    @Test
    void cancelEnrollment_fail() throws Exception {
        Account account = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", account);
        Event event = createEvent("event_title", EventType.FIRST_COME_FIRST_SERVED, 5, study, account);

        assertThrows(NestedServletException.class, () ->
                mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disEnroll")
                        .with(csrf()))
        );
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 수락 - POST 성공 (관리자 본인이 수락)")
    @Test
    void acceptEnrollment_success_manager() throws Exception {
        Study study = studyService.findPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, account);

        assertTrue(eventService.newEnrollment(study, event, account));
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertFalse(enrollment.isAccepted());

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 수락 - POST 성공 (회원 수락)")
    @Test
    void acceptEnrollment_success_member() throws Exception {
        Study study = studyService.findPath("path");
        Account manager = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        assertTrue(eventService.newEnrollment(study, event, manager));
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, manager);
        assertFalse(enrollment.isAccepted());

        Account member = accountFactory.createAccount("member");

        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));
        Enrollment enrollment_member = enrollmentRepository.findByEventAndAccount(event, member);
        assertFalse(enrollment_member.isAccepted());

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment_member.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollment_member.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 수락 - POST 실패 (관리자 권한이 아닌 다른 유저가 수락할 경우")
    @Test
    void acceptEnrollment_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("new-path", manager);
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountRepository.findByUsername("user");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, member);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/accept")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 취소 - POST 성공 (매니저 본인 취소)")
    @Test
    void rejectEnrollment_success_manager() throws Exception {
        Study study = studyService.findPath("path");
        Account account = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, account);

        assertTrue(eventService.newEnrollment(study, event, account));
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        enrollment.setAccepted(true);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/reject")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertFalse(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 취소 - POST 성공 (매니저 본인 취소)")
    @Test
    void rejectEnrollment_success_member() throws Exception {
        Study study = studyService.findPath("path");
        Account manager = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountFactory.createAccount("member");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));
        Enrollment enrollment_member = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment_member.setAccepted(true);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment_member.getId() + "/reject")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertFalse(enrollment_member.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 참가 신청 취소 - POST 실패 (관리자 권한이 아닌 다른 유저가 취소할 경우")
    @Test
    void rejectEnrollment_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", manager);
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountRepository.findByUsername("user");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment.setAccepted(true);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/reject")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(enrollment.isAccepted());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 - POST 성공 (매니저 본인)")
    @Test
    void checkInEnrollment_success_manager() throws Exception {
        Study study = studyService.findPath("path");
        Account user = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, user);

        assertTrue(eventService.newEnrollment(study, event, user));
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, user);
        enrollment.setAccepted(true);
        assertFalse(enrollment.isAttended());

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/check-in")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollment.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 - POST 성공 (회원)")
    @Test
    void checkInEnrollment_success_member() throws Exception {
        Study study = studyService.findPath("path");
        Account manager = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountFactory.createAccount("member");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));
        Enrollment enrollment_member = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment_member.setAccepted(true);
        assertFalse(enrollment_member.isAttended());

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment_member.getId() + "/check-in")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollment_member.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 - POST 실패 (관리자 권한이 아닌 다른 회원이 출석 할 경우)")
    @Test
    void checkInEnrollment_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("new-path", manager);
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountRepository.findByUsername("user");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment.setAccepted(true);
        assertFalse(enrollment.isAttended());

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/check-in")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(enrollment.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 취소 - POST 성공 (매니저 본인)")
    @Test
    void cancelCheckInEnrollment_success_manager() throws Exception {
        Study study = studyService.findPath("path");
        Account user = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, user);

        assertTrue(eventService.newEnrollment(study, event, user));
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, user);
        enrollment.setAccepted(true);
        enrollment.setAttended(true);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/cancel-check-in")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertFalse(enrollment.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 취소 - POST 성공 (회원)")
    @Test
    void cancelCheckInEnrollment_success_member() throws Exception {
        Study study = studyService.findPath("path");
        Account manager = accountRepository.findByUsername("user");
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountFactory.createAccount("member");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));
        Enrollment enrollment_member = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment_member.setAccepted(true);
        enrollment_member.setAttended(true);

        mvc.perform(post("/study/path/events/" + event.getId() + "/enrollments/" + enrollment_member.getId() + "/cancel-check-in")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertFalse(enrollment_member.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 권한 모임 신청 수락 후, 출석 체크 취소 - 실패 (관리자 권한이 아닌 다른 유저가 체크 할 경우)")
    @Test
    void cancelCheckInEnrollment_fail() throws Exception {
        Account manager = accountFactory.createAccount("manager");
        Study study = studyFactory.createStudy("test-path", manager);
        Event event = createEvent("event_title", EventType.MANAGER_CHECK, 5, study, manager);

        Account member = accountRepository.findByUsername("user");
        studyService.addMember(member, study);
        assertTrue(eventService.newEnrollment(study, event, member));

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, member);
        enrollment.setAccepted(true);
        enrollment.setAttended(true);

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enrollments/" + enrollment.getId() + "/cancel-check-in")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(enrollment.isAttended());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("선착순 모임 수정 - POST 성공 (인원 제한을 늘리면 인원에 포함되는 대기자는 자동 수락 되어야 한다.)")
    @Test
    public void updateLimitOfEnrollments() throws Exception {
        Account manager = accountRepository.findByUsername("user");
        Study study = studyService.findPath("path");
        Event event = createEvent("event-title", EventType.FIRST_COME_FIRST_SERVED, 2, study, manager);

        Account member1 = accountFactory.createAccount("member1");
        Account member2 = accountFactory.createAccount("member2");
        Account member3 = accountFactory.createAccount("member3");

        studyService.addMember(member1, study);
        studyService.addMember(member2, study);
        studyService.addMember(member3, study);

        eventService.newEnrollment(study, event, manager);
        eventService.newEnrollment(study, event, member1);
        eventService.newEnrollment(study, event, member2);
        eventService.newEnrollment(study, event, member3);

        Enrollment enrollment_manager = enrollmentRepository.findByEventAndAccount(event, manager);
        Enrollment enrollment_member1 = enrollmentRepository.findByEventAndAccount(event, member1);
        Enrollment enrollment_member2 = enrollmentRepository.findByEventAndAccount(event, member2);
        Enrollment enrollment_member3 = enrollmentRepository.findByEventAndAccount(event, member3);

        assertTrue(enrollment_manager.isAccepted());
        assertTrue(enrollment_member1.isAccepted());
        assertTrue(!enrollment_member2.isAccepted());
        assertTrue(!enrollment_member3.isAccepted());

        mvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/edit")
                        .param("title", "test-title")
                        .param("limitOfEnrollments", String.valueOf(3))
                        .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusDays(1)))
                        .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                        .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(7)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollment_manager.isAccepted());
        assertTrue(enrollment_member1.isAccepted());
        assertTrue(enrollment_member2.isAccepted());
        assertTrue(!enrollment_member3.isAccepted());
    }
}

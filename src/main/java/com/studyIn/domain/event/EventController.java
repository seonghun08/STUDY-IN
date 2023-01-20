package com.studyIn.domain.event;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.event.repository.EventRepository;
import com.studyIn.domain.study.dto.StudyQueryDto;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.study.service.StudyService;
import com.studyIn.domain.study.service.StudySettingsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final StudySettingsService studySettingsService;
    private final EventService eventService;
    private final StudyQueryRepository studyQueryRepository;
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventFormValidator eventFormValidator;

    @InitBinder("eventForm")
    public void eventFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventFormValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEvent(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                           @Valid EventForm eventForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
            model.addAttribute(accountInfo);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(accountInfo, path, eventForm);
        return "redirect:/study/" + path + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                            @PathVariable("id") Long eventId, Model model) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        Event event = eventRepository.findViewEventById(eventId);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute("event", event);
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentAccount AccountInfo accountInfo, @PathVariable String path, Model model) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        List<Event> events = eventRepository.findViewEventsByStudyId(study.getStudyId());

        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        events.forEach(event -> {
            if (event.getEndDate().isBefore(LocalDateTime.now())) {
                oldEvents.add(event);
            } else {
                newEvents.add(event);
            }
        });

        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                  @PathVariable("id") Long eventId, Model model) {
        StudyQueryDto study = studySettingsService.getStudyPermissionToUpdate(accountInfo, path);
        Event event = eventRepository.findViewEventById(eventId);
        model.addAttribute(accountInfo);
        model.addAttribute("study", study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                              @PathVariable("id") Long eventId, @Valid EventForm eventForm, Errors errors, Model model) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventFormValidator.validateUpdateForm(eventForm, eventId, errors);

        if (errors.hasErrors()) {
            StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
            Event event = eventRepository.findViewEventById(eventId);
            model.addAttribute(accountInfo);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(eventForm, eventId);
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                              @PathVariable("id") Long eventId) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventService.deleteEvent(eventId);
        return "redirect:/study/" + path + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                @PathVariable("id") Long eventId, RedirectAttributes attributes) {
        if (!eventService.newEnrollment(accountInfo, eventId)) {
            attributes.addFlashAttribute("message", "스터디 가입을 먼저 해주세요.");
            return "redirect:/study/" + path + "/events/" + eventId;
        }

        attributes.addFlashAttribute("message", "참가 신청이 완료되었습니다.");
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @PostMapping("/events/{id}/disEnroll")
    public String cancelEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                   @PathVariable("id") Long eventId, RedirectAttributes attributes) {
        eventService.cancelEnrollment(accountInfo, eventId);
        attributes.addFlashAttribute("message", "참가 신청이 취소되었습니다.");
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @PostMapping("/events/{id}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                   @PathVariable("id") Long eventId, @PathVariable("enrollmentId") Long enrollmentId) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventService.accept(eventId, enrollmentId);
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @PostMapping("/events/{id}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                   @PathVariable("id") Long eventId, @PathVariable("enrollmentId") Long enrollmentId) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventService.reject(eventId, enrollmentId);
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @PostMapping("events/{id}/enrollments/{enrollmentId}/check-in")
    public String checkInEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                    @PathVariable("id") Long eventId, @PathVariable("enrollmentId") Long enrollmentId) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventService.checkIn(eventId, enrollmentId);
        return "redirect:/study/" + path + "/events/" + eventId;
    }

    @PostMapping("events/{id}/enrollments/{enrollmentId}/cancel-check-in")
    public String cancelCheckInEnrollment(@CurrentAccount AccountInfo accountInfo, @PathVariable String path,
                                          @PathVariable("id") Long eventId, @PathVariable("enrollmentId") Long enrollmentId) {
        studyService.checkPermissions(path, accountInfo.getAccountId());
        eventService.cancelCheckIn(eventId, enrollmentId);
        return "redirect:/study/" + path + "/events/" + eventId;
    }
}

package com.studyIn.modules.event;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.CurrentAccount;
import com.studyIn.modules.event.form.EventForm;
import com.studyIn.modules.event.validator.EventFormValidator;
import com.studyIn.modules.study.Study;
import com.studyIn.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;

    private final ModelMapper modelMapper;
    private final EventFormValidator eventFormValidator;

    @InitBinder("eventForm")
    public void eventFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventFormValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEvent(@CurrentAccount Account account, @PathVariable String path,
                           @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(study, account, modelMapper.map(eventForm, Event.class));
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@CurrentAccount Account account, @PathVariable String path,
                            @PathVariable("id") Event event, Model model) {
        Study study = studyService.findPath(path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.findPath(path);
        EventList eventList = eventService.findEventsOrderByNewEventAndOldEvent(study);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("newEvents", eventList.getNewEvents());
        model.addAttribute("oldEvents", eventList.getOldEvents());
        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account, @PathVariable String path,
                                  @PathVariable("id") Event event, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@CurrentAccount Account account, @PathVariable String path,
                              @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventFormValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(eventForm, event);
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@CurrentAccount Account account, @PathVariable String path,
                              @PathVariable("id") Event event) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                @PathVariable("id") Event event, RedirectAttributes attributes) {
        Study study = studyService.findPathWithMembersAndManagers(path);

        if (!eventService.newEnrollment(study, event, account)) {
            attributes.addFlashAttribute("message", "스터디 가입을 먼저 해주세요.");
            return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
        }

        attributes.addFlashAttribute("message", "참가 신청이 완료되었습니다.");
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disEnroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("id") Event event, RedirectAttributes attributes) {
        Study study = studyService.findPathWithMembersAndManagers(path);
        eventService.cancelEnrollment(study, event, account);
        attributes.addFlashAttribute("message", "참가 신청이 취소되었습니다.");
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("id") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventService.accept(event, enrollment);
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("id") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventService.reject(event, enrollment);
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @PostMapping("events/{id}/enrollments/{enrollmentId}/check-in")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("id") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventService.checkIn(event, enrollment);
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }

    @PostMapping("events/{id}/enrollments/{enrollmentId}/cancel-check-in")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("id") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        eventService.cancelChekIn(event, enrollment);
        return "redirect:/study/" + study.getPath() + "/events/" + event.getId();
    }
}

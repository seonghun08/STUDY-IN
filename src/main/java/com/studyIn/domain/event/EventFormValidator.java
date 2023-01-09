package com.studyIn.domain.event;

import com.studyIn.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventFormValidator implements Validator {

    private final EventRepository eventRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "valid.datetime", "모임 접수 종료일을 정확히 입력해주세요.");
        }

        if (isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime", "valid.datetime", "모임 시작일을 정확히 입력해주세요.");
        }

        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime", "valid.datetime", "모임 종료일을 정확히 입력해주세요.");
        }
    }

    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDate().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDate().isBefore(eventForm.getEndEnrollmentDate());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        return eventForm.getEndDate().isBefore(eventForm.getStartDate()) || eventForm.getEndDate().isBefore(eventForm.getEndEnrollmentDate());
    }

    public void validateUpdateForm(EventForm eventForm, Long eventId, Errors errors) {
        Event event = eventRepository.findWithEnrollmentsById(eventId)
                .orElseThrow(IllegalArgumentException::new);

        if (event.getAcceptedEnrollmentCount() > eventForm.getLimitOfEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "valid.limitOfEnrollments", "확인된 참가 신청 인원보다 모집 인원 수가 더 커야 합니다.");
        }

        if (eventForm.getLimitOfEnrollments() <= 2) {
            errors.rejectValue("limitOfEnrollments", "valid.limitOfEnrollments", "최소 2인 이상 모임이어야 합니다.");
        }
    }
}


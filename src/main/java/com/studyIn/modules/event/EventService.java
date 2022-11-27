package com.studyIn.modules.event;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.event.event.EnrollmentAcceptedEvent;
import com.studyIn.modules.event.event.EnrollmentRejectedEvent;
import com.studyIn.modules.event.form.EventForm;
import com.studyIn.modules.study.Study;
import com.studyIn.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Event findEvent(Long id) {
        return eventRepository.findById(id).orElseThrow();
    }

    public EventList findEventsOrderByNewEventAndOldEvent(Study study) {
        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        EventList eventList = new EventList();

        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                eventList.getOldEvents().add(e);
            } else {
                eventList.getNewEvents().add(e);
            }
        });

        return eventList;
    }

    public List<Enrollment> getEnrollmentList(Account account, boolean accepted) {
        List<Enrollment> enrollments = enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(account, accepted);
        List<Enrollment> enrollmentList = new ArrayList<>();

        enrollments.forEach(enrollment -> {
            if (enrollment.getEvent().getEndDateTime().isAfter(LocalDateTime.now())) {
                enrollmentList.add(enrollment);
            }
        });

        return enrollmentList;
    }


    public Event createEvent(Study study, Account account, Event event) {
        event.setStudy(study);
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    public void updateEvent(EventForm eventForm, Event event) {
        eventForm.setEventType(event.getEventType());
        modelMapper.map(eventForm, event);
        event.acceptedWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임 정보가 수정되었으니 확인하세요."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(findEvent(event.getId()));
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 취소되었습니다."));
    }

    public boolean newEnrollment(Study study, Event event, Account account) {
        if (!study.isMemberOrManager(account)) {
            return false;
        }

        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccount(account);
            enrollment.setAccepted(event.checkAcceptedEnrollment());
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
            return true;

        } else {
            return false;
        }

    }

    public void cancelEnrollment(Study study, Event event, Account account) {
        if (!study.isMemberOrManager(account)) {
            throw new IllegalArgumentException();
        }

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        event.cancelEnrollment(enrollment);
        enrollmentRepository.delete(enrollment);
        event.acceptNextWaitingEnrollment();
    }


    public void accept(Event event, Enrollment enrollment) {
        isContainEnrollment(event, enrollment);
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void reject(Event event, Enrollment enrollment) {
        isContainEnrollment(event, enrollment);
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkIn(Event event, Enrollment enrollment) {
        isContainEnrollment(event, enrollment);
        if (!enrollment.checkIn()) {
            throw new IllegalArgumentException();
        }
    }

    public void cancelChekIn(Event event, Enrollment enrollment) {
        isContainEnrollment(event, enrollment);
        if (!enrollment.cancelCheckIn()) {
            throw new IllegalArgumentException();
        }
    }

    private void isContainEnrollment (Event event, Enrollment enrollment) {
        if (!event.getEnrollments().contains(enrollment)) {
            throw new IllegalArgumentException();
        }
    }
}

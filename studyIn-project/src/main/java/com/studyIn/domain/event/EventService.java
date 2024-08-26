package com.studyIn.domain.event;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.event.repository.EventRepository;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.event.StudyUpdateEvent;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    private void checkPermissions(AccountInfo accountInfo, Study study) {
        if (!study.getManagers().stream()
                .map(m -> m.getAccount().getUsername())
                .collect(Collectors.toList())
                .contains(accountInfo.getUsername())) {
            throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
        }
    }

    public Event createEvent(AccountInfo accountInfo, String path, EventForm eventForm) {
        Study study = studyRepository.findWithManagersByPath(path)
                .orElseThrow(IllegalArgumentException::new);
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);

        checkPermissions(accountInfo, study);
        Event event = Event.createEvent(eventForm, study, account);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    public void updateEvent(EventForm eventForm, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(IllegalArgumentException::new);
        event.eventUpdate(eventForm);
        event.acceptedWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임 정보가 수정되었으니 확인하세요."));
    }

    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
//        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 취소되었습니다."));
    }

    public boolean newEnrollment(AccountInfo accountInfo, Long eventId) {
        Account account = accountRepository.findById(accountInfo.getAccountId()).orElseThrow(IllegalArgumentException::new);
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);

        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledDate(LocalDateTime.now());
            enrollment.setAccount(account);
            enrollment.setAccepted(event.checkAcceptedEnrollment());
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
            return true;

        } else {
            return false;
        }
    }

    public void cancelEnrollment(AccountInfo accountInfo, Long eventId) {
        Account account = accountRepository.findById(accountInfo.getAccountId()).orElseThrow(IllegalArgumentException::new);
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        event.cancelEnrollment(enrollment);
        event.acceptNextWaitingEnrollment();
        enrollmentRepository.delete(enrollment);
    }

    public void accept(Long eventId, Long enrollmentId) {
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(IllegalArgumentException::new);
        isContainEnrollment(event, enrollment);
        event.accept(enrollment);
    }

    public void reject(Long eventId, Long enrollmentId) {
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(IllegalArgumentException::new);
        isContainEnrollment(event, enrollment);
        event.reject(enrollment);
    }

    public void checkIn(Long eventId, Long enrollmentId) {
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(IllegalArgumentException::new);
        isContainEnrollment(event, enrollment);
        if (!enrollment.checkIn()) {
            throw new IllegalArgumentException();
        }
    }

    public void cancelCheckIn(Long eventId, Long enrollmentId) {
        Event event = eventRepository.findById(eventId).orElseThrow(IllegalArgumentException::new);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(IllegalArgumentException::new);
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

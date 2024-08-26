package com.studyIn.modules.event.event;

import com.studyIn.infra.config.AppProperties;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import com.studyIn.modules.account.Account;
import com.studyIn.modules.event.Enrollment;
import com.studyIn.modules.event.Event;
import com.studyIn.modules.notification.Notification;
import com.studyIn.modules.notification.NotificationRepository;
import com.studyIn.modules.notification.NotificationType;
import com.studyIn.modules.study.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Async
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        if (account.isStudyEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, study);
        }

        if (account.isStudyEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, event, study);
        }

    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Context context = new Context();
        context.setVariable("link", "/study/" + study.getPath() + "/events/" + event.getId());
        context.setVariable("username", account.getUsername());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/mail-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("STUDY IN, " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle() + " :: " + event.getTitle());
        notification.setLink("/study/" + study.getPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }
}

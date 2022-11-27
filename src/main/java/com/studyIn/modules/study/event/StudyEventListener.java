package com.studyIn.modules.study.event;

import com.studyIn.infra.config.AppProperties;
import com.studyIn.infra.mail.EmailMessage;
import com.studyIn.infra.mail.EmailService;
import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.AccountPredicates;
import com.studyIn.modules.account.AccountRepository;
import com.studyIn.modules.notification.Notification;
import com.studyIn.modules.notification.NotificationRepository;
import com.studyIn.modules.notification.NotificationType;
import com.studyIn.modules.study.Study;
import com.studyIn.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;


    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndLocationsById(studyCreatedEvent.getStudy().getId());

        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndLocations(study.getTags(), study.getLocations()));
        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendEmail(study, account, "새로운 스터디가 생겼습니다.",
                        "STUDY IN, '" + study.getTitle() + "' 스터디가 생겼습니다.");
            }

            if (account.isStudyCreatedByWeb()) {
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersByPath(studyUpdateEvent.getStudy().getPath());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {
                sendEmail(study, account, studyUpdateEvent.getMessage(),
                        "STUDY IN, '" + study.getTitle() + "' 스터디에 새소식이 있습니다.");
            }

            if (account.isStudyUpdatedByWeb()) {
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });
    }

    private void sendEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("link", "/study/" + study.getPath());
        context.setVariable("username", account.getUsername());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/mail-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getPath());
        notification.setMessage(message);
        notification.setChecked(false);
        notification.setAccount(account);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }
}

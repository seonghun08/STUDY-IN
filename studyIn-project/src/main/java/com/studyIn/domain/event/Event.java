package com.studyIn.domain.event;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.UserAccount;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.study.entity.Study;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "event")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String description;

    @Column(nullable = false)
    private int limitOfEnrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDate;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account createdBy;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.ALL)
    @OrderBy("enrolledDate")
    private List<Enrollment> enrollments = new ArrayList<>();

    private int enrollmentCount;


    //== 생성 메서드 ==//
    public static Event createEvent(EventForm eventForm, Study study, Account account) {
        Event event = new Event();
        event.title = eventForm.getTitle();
        event.description = eventForm.getDescription();
        event.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        event.eventType = eventForm.getEventType();
        event.endEnrollmentDate = eventForm.getEndEnrollmentDate();
        event.startDate = eventForm.getStartDate();
        event.endDate = eventForm.getEndDate();

        event.study = study;
        event.createdBy = account;
        event.createdDate = LocalDateTime.now();
        return event;
    }

    /**
     * Service method
     */
    public long currentProgress() {
        long totalTime = ChronoUnit.DAYS.between(this.startDate, this.endDate);
        long pastTime = ChronoUnit.DAYS.between(this.startDate, LocalDateTime.now());
        long currentProgress = (long) (((double) pastTime / totalTime) * 100);
        return currentProgress > 0 ? currentProgress : 0;
    }

    public boolean isEventStarted() {
        return this.startDate.isAfter(LocalDateTime.now());
    }

    public boolean isEndedEnrollment() {
        return this.endEnrollmentDate.isAfter(LocalDateTime.now());
    }

    public boolean isEnrollAbleFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisEnrollAbleFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    public boolean isAttended(UserAccount userAccount) {
        AccountInfo accountInfo = userAccount.getAccountInfo();

        for (Enrollment e : this.enrollments) {
            if (e.getAccount().getUsername().equals(accountInfo.getUsername()) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public int numberOfRemainSpots() {
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDate.isAfter(LocalDateTime.now());
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        AccountInfo accountInfo = userAccount.getAccountInfo();

        for (Enrollment e : this.enrollments) {
            if (e.getAccount().getUsername().equals(accountInfo.getUsername())) {
                return true;
            }
        }

        return false;
    }

    public Long getAcceptedEnrollmentCount() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void acceptedWaitingList() {
        var waitingList = waitingListEnrollment();
        int remainingAcceptCount = (int) (this.getLimitOfEnrollments() - this.getAcceptedEnrollmentCount());
        int acceptedNumber = Math.min(remainingAcceptCount, waitingList.size());
        waitingList.subList(0, acceptedNumber).forEach(e -> e.setAccepted(true));
    }

    public void acceptNextWaitingEnrollment() {
        if (this.checkAcceptedEnrollment()) {
            Enrollment enrollment = this.getFirstWaitingEnrollment();
            if (enrollment != null) {
                enrollment.setAccepted(true);
            }
        }
    }

    public boolean checkAcceptedEnrollment() {
        return this.eventType == EventType.FIRST_COME_FIRST_SERVED && this.limitOfEnrollments > getAcceptedEnrollmentCount();
    }

    private List<Enrollment> waitingListEnrollment() {
        return this.getEnrollments().stream().filter(e -> !e.isAccepted()).collect(Collectors.toList());
    }

    private Enrollment getFirstWaitingEnrollment() {
        for (Enrollment e : this.enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }
        return null;
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);

        if (this.eventType == EventType.FIRST_COME_FIRST_SERVED) {
            this.enrollmentCount++;
        }

        enrollment.setEvent(this);
    }

    public void cancelEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        this.enrollmentCount--;
        enrollment.setEvent(null);
    }

    public boolean canAccept(Enrollment enrollment) {
        return this.eventType == EventType.MANAGER_CHECK
                && this.enrollments.contains(enrollment)
                && !enrollment.isAccepted()
                && !enrollment.isAttended();
    }

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.MANAGER_CHECK
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    public void accept(Enrollment enrollment) {
        if (this.getEventType() == EventType.MANAGER_CHECK && this.limitOfEnrollments > getAcceptedEnrollmentCount()) {
            enrollment.setAccepted(true);
            this.enrollmentCount++;
        }
    }

    public void reject(Enrollment enrollment) {
        if (this.getEventType() == EventType.MANAGER_CHECK) {
            enrollment.setAccepted(false);
            this.enrollmentCount--;
        }
    }

    public void eventUpdate(EventForm eventForm) {
        this.title = eventForm.getTitle();
        this.description = eventForm.getDescription();
        this.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        this.endEnrollmentDate = eventForm.getEndEnrollmentDate();
        this.startDate = eventForm.getStartDate();
        this.endDate = eventForm.getEndDate();
    }
}

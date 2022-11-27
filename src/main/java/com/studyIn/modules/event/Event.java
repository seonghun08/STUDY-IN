package com.studyIn.modules.event;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.UserAccount;
import com.studyIn.modules.study.Study;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.EAGER;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column
    private Integer limitOfEnrollments;

    @OneToMany(fetch = EAGER, mappedBy = "event")
    @OrderBy("enrolledAt")
    private List<Enrollment> enrollments = new ArrayList<>();

    private int enrollmentCount;

    @Enumerated(EnumType.STRING)
    private EventType eventType;


    /**
     * Service method
     */
    public long currentProgress() {
        long totalTime = ChronoUnit.DAYS.between(this.startDateTime, this.endDateTime);
        long pastTime = ChronoUnit.DAYS.between(this.startDateTime, LocalDateTime.now());
        long currentProgress = (long) (((double) pastTime / totalTime) * 100);
        return currentProgress;
    }

    public boolean isEventStarted() {
        return this.startDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isEndedEnrollment() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isEnrollAbleFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisEnrollAbleFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();

        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public int numberOfRemainSpots() {
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();

        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
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

}

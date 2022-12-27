package com.studyIn.domain.account;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class NotificationSettings {

    private boolean studyCreatedByEmail;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyUpdatedByEmail;

    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByWeb;

    /**
     * 임시
     */
    public NotificationSettings() {
        this.studyCreatedByEmail = false;
        this.studyEnrollmentResultByEmail = false;
        this.studyUpdatedByEmail = false;
        this.studyCreatedByWeb = true;
        this.studyEnrollmentResultByWeb = true;
        this.studyUpdatedByWeb = true;
    }
}

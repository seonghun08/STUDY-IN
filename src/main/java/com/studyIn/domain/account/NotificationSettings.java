package com.studyIn.domain.account;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class NotificationSettings {

    /**
     * email 알림 설정
     */
    private boolean studyCreatedByEmail;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyUpdatedByEmail;

    /**
     * web 알림 설정
     */
    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByWeb;

    /**
     * 임시 생성자
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

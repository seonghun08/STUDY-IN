package com.studyIn.domain.account.dto.form;

import lombok.Data;

@Data
public class NotificationsSettingForm {

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
}

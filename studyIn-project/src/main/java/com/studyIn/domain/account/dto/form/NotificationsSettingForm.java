package com.studyIn.domain.account.dto.form;

import lombok.Data;

@Data
public class NotificationsSettingForm {

    /**
     * email settings
     */
    private boolean studyCreatedByEmail;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyUpdatedByEmail;

    /**
     * web settings
     */
    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByWeb;
}

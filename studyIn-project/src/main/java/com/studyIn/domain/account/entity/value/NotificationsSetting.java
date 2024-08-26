package com.studyIn.domain.account.entity.value;

import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class NotificationsSetting {

    /**
     * email settings
     */
    private boolean studyCreatedByEmail;
    private boolean studyUpdatedByEmail;
    private boolean studyEnrollmentResultByEmail;


    /**
     * web settings
     */
    private boolean studyCreatedByWeb;
    private boolean studyUpdatedByWeb;
    private boolean studyEnrollmentResultByWeb;

    /**
     * 임시 생성자
     */
    public NotificationsSetting() {
        this.studyCreatedByEmail = false;
        this.studyUpdatedByEmail = false;
        this.studyEnrollmentResultByEmail = false;

        this.studyCreatedByWeb = true;
        this.studyUpdatedByWeb = true;
        this.studyEnrollmentResultByWeb = true;
    }

    //== 수정 메서드 ==//
    public void update(NotificationsSettingForm notificationsSettingForm) {
        this.studyCreatedByEmail = notificationsSettingForm.isStudyCreatedByEmail();
        this.studyUpdatedByEmail = notificationsSettingForm.isStudyUpdatedByEmail();
        this.studyEnrollmentResultByEmail = notificationsSettingForm.isStudyEnrollmentResultByEmail();

        this.studyCreatedByWeb = notificationsSettingForm.isStudyCreatedByWeb();
        this.studyUpdatedByWeb = notificationsSettingForm.isStudyUpdatedByWeb();
        this.studyEnrollmentResultByWeb = notificationsSettingForm.isStudyEnrollmentResultByWeb();
    }
}

package com.studyIn.modules.event.event;

import com.studyIn.modules.event.Enrollment;

public class EnrollmentAcceptedEvent extends EnrollmentEvent {

    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청이 수락되었습니다. 모임을 확인해주세요.");
    }
}

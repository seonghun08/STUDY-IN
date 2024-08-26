package com.studyIn.modules.study.event;

import com.studyIn.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;
}

package com.studyIn.domain.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.entity.QAccount;
import com.studyIn.domain.account.entity.QProfile;
import com.studyIn.domain.event.Event;
import com.studyIn.domain.event.QEnrollment;
import com.studyIn.domain.event.QEvent;
import com.studyIn.domain.study.entity.QStudy;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.studyIn.domain.account.entity.QAccount.*;
import static com.studyIn.domain.account.entity.QProfile.profile;
import static com.studyIn.domain.event.QEnrollment.enrollment;
import static com.studyIn.domain.event.QEvent.event;
import static com.studyIn.domain.study.entity.QStudy.study;

@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Event findViewEventById(Long id) {
        Event findEvent = jpaQueryFactory
                .selectFrom(event)
                .leftJoin(event.study, study).fetchJoin()
                .leftJoin(event.createdBy, account).fetchJoin()
                .leftJoin(event.enrollments, enrollment).fetchJoin()
                .leftJoin(enrollment.account, account).fetchJoin()
                .rightJoin(account.profile, profile).fetchJoin()
                .where(event.id.eq(id))
                .fetchOne();
        return findEvent;
    }

    @Override
    public List<Event> findViewEventsByStudyId(Long studyId) {
        return jpaQueryFactory
                .selectFrom(event)
                .leftJoin(event.enrollments, enrollment).fetchJoin()
                .join(event.study, study)
                .where(study.id.eq(studyId))
                .distinct()
                .fetch();
    }
}

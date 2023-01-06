package com.studyIn.domain.study.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.study.entity.*;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QProfile.profile;
import static com.studyIn.domain.location.QLocation.location;
import static com.studyIn.domain.study.entity.QStudy.study;
import static com.studyIn.domain.study.entity.QStudyLocation.studyLocation;
import static com.studyIn.domain.study.entity.QStudyManager.studyManager;
import static com.studyIn.domain.study.entity.QStudyMember.studyMember;
import static com.studyIn.domain.study.entity.QStudyTag.studyTag;
import static com.studyIn.domain.tag.QTag.tag;

@RequiredArgsConstructor
public class StudyRepositoryCustomImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Study> findByPath(String path) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(study)
                .leftJoin(study.managers, studyManager).fetchJoin()
                .leftJoin(studyManager.account, account).fetchJoin()
                .leftJoin(account.profile, profile).fetchJoin()
                .leftJoin(study.members, studyMember).fetchJoin()
                .leftJoin(studyMember.account, account).fetchJoin()
                .leftJoin(account.profile, profile).fetchJoin()
                .leftJoin(study.studyTags, studyTag).fetchJoin()
                .leftJoin(studyTag.tag, tag).fetchJoin()
                .leftJoin(study.studyLocations, studyLocation).fetchJoin()
                .leftJoin(studyLocation.location, location).fetchJoin()
                .where(study.path.eq(path))
                .fetchOne());
    }
}

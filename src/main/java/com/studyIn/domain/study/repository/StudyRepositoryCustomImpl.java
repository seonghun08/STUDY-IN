package com.studyIn.domain.study.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.study.entity.QStudy;
import com.studyIn.domain.study.entity.Study;
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

    /**
     * 엔티티 내에 컬렉션을 Set<>으로 바꿔야 하는 문제
     * 이러한 방식은 성능 저하 이슈가 일어날 확률이 높음
     * XxxToMany 인 경우 각각의 컬렉션마다 쿼리를 보내는게 낫다.
     * 사용 X
     */
    @Override
    public Optional<Study> findAllByPath(String path) {
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

    @Override
    public Optional<Study> findWithManagersByPath(String path) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(QStudy.study)
                .join(QStudy.study.managers, studyManager).fetchJoin()
                .join(studyManager.account, account).fetchJoin()
                .where(QStudy.study.path.eq(path))
                .fetchOne());
    }
}

package com.studyIn.domain.study.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QAuthentication.authentication;
import static com.studyIn.domain.account.entity.QProfile.profile;
import static com.studyIn.domain.location.QLocation.location;
import static com.studyIn.domain.study.entity.QStudy.study;
import static com.studyIn.domain.study.entity.QStudyLocation.studyLocation;
import static com.studyIn.domain.study.entity.QStudyManager.studyManager;
import static com.studyIn.domain.study.entity.QStudyMember.studyMember;
import static com.studyIn.domain.study.entity.QStudyTag.studyTag;
import static com.studyIn.domain.tag.QTag.tag;

@Repository
@RequiredArgsConstructor
public class StudyQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public StudyQueryDto findStudyQueryDtoByPath(String path) {
        StudyQueryDto studyQueryDto = jpaQueryFactory
                .select(Projections.constructor(
                        StudyQueryDto.class,
                        study.id.as("studyId"),
                        study.path,
                        study.title,
                        study.shortDescription,
                        study.fullDescription,
                        study.published,
                        study.recruiting,
                        study.closed,
                        study.bannerImage,
                        study.useBanner
                ))
                .from(study)
                .where(study.path.eq(path))
                .fetchOne();

        if (studyQueryDto == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 존재하지 않습니다.");
        }

        Long studyId = studyQueryDto.getStudyId();
        studyQueryDto.setManagers(findStudyMangers(studyId));
        studyQueryDto.setMembers(findStudyMembers(studyId));
        studyQueryDto.setTagTitles(findStudyTagTitles(studyId));
        studyQueryDto.setLocationTitles(findStudyLocationTitles(studyId));
        return studyQueryDto;
    }

    private List<String> findStudyLocationTitles(Long studyId) {
        List<String> locationTitles = jpaQueryFactory
                .select(location.localNameOfCity)
                .from(studyLocation)
                .join(studyLocation.study, study)
                .join(studyLocation.location, location)
                .where(studyLocation.study.id.eq(studyId))
                .fetch();
        return locationTitles;
    }

    private List<String> findStudyTagTitles(Long studyId) {
        List<String> tagTitles = jpaQueryFactory
                .select(tag.title)
                .from(studyTag)
                .join(studyTag.study, study)
                .join(studyTag.tag, tag)
                .where(studyTag.study.id.eq(studyId))
                .fetch();
        return tagTitles;
    }

    private List<StudyAccountDto> findStudyMembers(Long studyId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        StudyAccountDto.class,
                        account.id.as("accountId"),
                        account.username,
                        authentication.email,
                        profile.nickname,
                        profile.profileImage,
                        profile.bio))
                .from(studyMember)
                .join(studyMember.study, study)
                .join(studyMember.account, account)
                .join(account.authentication, authentication)
                .join(account.profile, profile)
                .where(studyMember.study.id.eq(studyId))
                .fetch();
    }

    private List<StudyAccountDto> findStudyMangers(Long studyId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        StudyAccountDto.class,
                        account.id.as("accountId"),
                        account.username,
                        authentication.email,
                        profile.nickname,
                        profile.profileImage,
                        profile.bio))
                .from(studyManager)
                .join(studyManager.study, study)
                .join(studyManager.account, account)
                .join(account.authentication, authentication)
                .join(account.profile, profile)
                .where(studyManager.study.id.eq(studyId))
                .fetch();
    }
}

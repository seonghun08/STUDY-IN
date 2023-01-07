package com.studyIn.domain.study.repository;

import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.tag.Tag;

import java.util.List;
import java.util.Optional;

public interface StudyRepositoryCustom {

    Optional<Study> findAllByPath(String path);

    Optional<Study> findWithManagersByPath(String path);

    Optional<Study> findWithMembersByPath(String path);

    Long deleteStudyTag(Long tagId, Long studyId);

    Long deleteStudyLocation(Long locationId, Long studyId);

    Long deleteStudyMember(Long accountId, Long studyId);
}

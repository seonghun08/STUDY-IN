package com.studyIn.domain.study.repository;

import com.studyIn.domain.study.entity.Study;

import java.util.Optional;

public interface StudyRepositoryCustom {

    Optional<Study> findByPath(String path);
}

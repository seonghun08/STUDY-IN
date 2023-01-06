package com.studyIn.domain.study.repository;

import com.studyIn.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {

    boolean existsByTitle(String title);

    boolean existsByPath(String path);
}

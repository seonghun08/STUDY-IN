package com.studyIn.modules.study;

import com.studyIn.modules.location.Location;
import com.studyIn.modules.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    Page<Study> findAll(Pageable pageable);

    Page<Study> findByTagsAndLocations(Set<Tag> tags, Set<Location> locations, Pageable pageable);
}

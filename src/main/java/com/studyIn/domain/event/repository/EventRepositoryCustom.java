package com.studyIn.domain.event.repository;

import com.studyIn.domain.event.Event;

import java.util.List;

public interface EventRepositoryCustom {

    Event findViewEventById(Long id);

    List<Event> findViewEventsByStudyId(Long studyId);
}

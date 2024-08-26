package com.studyIn.domain.event.repository;

import com.studyIn.domain.event.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    @EntityGraph(attributePaths = {"enrollments"})
    Optional<Event> findWithEnrollmentsById(Long eventId);
}

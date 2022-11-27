package com.studyIn.modules.study;

import com.studyIn.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    boolean existsByTitle(String title);

    boolean existsByPath(String path);

    @EntityGraph(value = "Study.withAll", type = LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Study findStudyWithTagsAndManagersByPath(String path);

    @EntityGraph(attributePaths = {"locations", "managers"})
    Study findStudyWithLocationsAndManagersByPath(String path);

    @EntityGraph(attributePaths = {"managers"})
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Study findStudyWithMembersByPath(String path);

    @EntityGraph(attributePaths = {"managers", "members"})
    Study findStudyWithManagersAndMembersByPath(String path);

    @EntityGraph(attributePaths = {"tags", "locations"})
    Study findStudyWithTagsAndLocationsById(Long id);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}

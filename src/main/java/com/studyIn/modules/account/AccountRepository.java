package com.studyIn.modules.account;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Account findByEmail(String email);

    Account findByUsername(String username);

    @EntityGraph(value = "Account.withAll", type = LOAD)
    Account findAccountWithAllById(Long Id);

    @EntityGraph(attributePaths = {"tags", "locations"})
    Account findAccountWithTagsAndLocationsById(Long id);
}

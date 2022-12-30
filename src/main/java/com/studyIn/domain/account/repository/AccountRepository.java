package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"profile", "authentication"})
    Optional<Account> findWithAllById(Long id);

    @EntityGraph(attributePaths = {"profile", "authentication"})
    Optional<Account> findWithAllByUsername(String username);

    @Query("select a from Account a join fetch a.profile p where a.id = :id")
    Optional<Account> findWithProfileById(@Param("id") Long id);
}

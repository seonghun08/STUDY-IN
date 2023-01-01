package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"authentication"})
    Optional<Account> findWithAuthenticationById(Long id);

    @Query("select a from Account a join fetch a.authentication au join fetch a.profile where a.username = :username")
    Optional<Account> findByUsername(@Param("username") String username);

    @Query("select a from Account a join fetch a.authentication au join fetch a.profile where au.email = :email")
    Optional<Account> findByEmail(@Param("email") String email);
}

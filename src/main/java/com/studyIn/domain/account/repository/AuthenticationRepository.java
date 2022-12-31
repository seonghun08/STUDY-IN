package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Authentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {

    boolean existsByEmail(String email);

    Optional<Authentication> findByEmail(String email);

    @Query("select au from Authentication au join fetch au.account a where au.email = :email")
    Optional<Authentication> findWithAccountByEmail(@Param("email") String email);
}

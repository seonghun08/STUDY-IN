package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

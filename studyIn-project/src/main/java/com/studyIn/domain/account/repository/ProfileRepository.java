package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}

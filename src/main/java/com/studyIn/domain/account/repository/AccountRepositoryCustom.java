package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.dto.ProfileDTO;

import java.util.Optional;

public interface AccountRepositoryCustom {

    Optional<ProfileDTO> findProfileDtoByUsername(String username);
}

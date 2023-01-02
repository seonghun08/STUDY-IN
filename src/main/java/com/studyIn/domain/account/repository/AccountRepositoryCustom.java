package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.dto.ProfileDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryCustom {

    Optional<ProfileDto> findProfileDtoByUsername(String username);

    List<String> findTagTitleByAccountId(@Param("id") Long accountId);

    String findPasswordByUsername(@Param("username") String username);
}

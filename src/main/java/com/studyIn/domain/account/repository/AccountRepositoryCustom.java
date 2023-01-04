package com.studyIn.domain.account.repository;

import com.studyIn.domain.account.dto.ProfileDto;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.tag.Tag;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryCustom {

    Optional<ProfileDto> findProfileDtoByUsername(String username);

    String findPasswordByUsername(String username);

    List<Tag> findTagListByAccountId(Long accountId);

    List<Location> findLocationListById(Long accountId);

    Long deleteAccountTag(Long tagId, Long accountId);

    Long deleteAccountLocation(Long locationId, Long accountId);
}

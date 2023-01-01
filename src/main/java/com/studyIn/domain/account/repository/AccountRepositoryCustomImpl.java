package com.studyIn.domain.account.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.dto.ProfileDTO;
import com.studyIn.domain.account.entity.QAccount;
import com.studyIn.domain.account.entity.QProfile;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QAuthentication.authentication;
import static com.studyIn.domain.account.entity.QProfile.profile;

@RequiredArgsConstructor
public class AccountRepositoryCustomImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<ProfileDTO> findProfileDtoByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        ProfileDTO.class,
                        account.username,
                        account.createdDate,
                        authentication.email,
                        authentication.emailVerified,
                        profile.nickname,
                        profile.occupation,
                        profile.location,
                        profile.bio,
                        profile.link,
                        profile.profileImage
                ))
                .from(account)
                .join(account.authentication, authentication)
                .join(account.profile, profile)
                .where(account.username.eq(username))
                .fetchOne()
        );
    }
}

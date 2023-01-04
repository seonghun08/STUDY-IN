package com.studyIn.domain.account.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.dto.ProfileDto;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.tag.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QAccountLocation.accountLocation;
import static com.studyIn.domain.account.entity.QAccountTag.accountTag;
import static com.studyIn.domain.account.entity.QAuthentication.authentication;
import static com.studyIn.domain.account.entity.QProfile.profile;
import static com.studyIn.domain.location.QLocation.location;
import static com.studyIn.domain.tag.QTag.tag;

@RequiredArgsConstructor
public class AccountRepositoryCustomImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<ProfileDto> findProfileDtoByUsername(String username) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(Projections.constructor(
                                ProfileDto.class,
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

    @Override
    public List<Tag> findTagListByAccountId(Long accountId) {
        return jpaQueryFactory
                .select(tag)
                .from(account)
                .join(account.accountTags, accountTag)
                .join(accountTag.tag, tag)
                .where(account.id.eq(accountId))
                .fetch();
    }

    @Override
    public List<Location> findLocationListById(Long accountId) {
        return jpaQueryFactory
                .select(location)
                .from(account)
                .join(account.accountLocations, accountLocation)
                .join(accountLocation.location, location)
                .where(account.id.eq(accountId))
                .fetch();
    }

    @Override
    public String findPasswordByUsername(String username) {
        return jpaQueryFactory
                .select(account.password)
                .from(account)
                .where(account.username.eq(username))
                .fetchOne();
    }

    @Override
    public Long deleteAccountTag(Long tagId, Long accountId) {
        return jpaQueryFactory
                .delete(accountTag)
                .where(accountTag.account.id.eq(accountId)
                        .and(accountTag.tag.id.eq(tagId)))
                .execute();
    }

    @Override
    public Long deleteAccountLocation(Long locationId, Long accountId) {
        return jpaQueryFactory
                .delete(accountLocation)
                .where(accountLocation.account.id.eq(accountId)
                        .and(accountLocation.location.id.eq(locationId)))
                .execute();
    }

}

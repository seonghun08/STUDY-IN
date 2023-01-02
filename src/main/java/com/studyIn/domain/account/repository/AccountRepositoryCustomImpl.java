package com.studyIn.domain.account.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QAccountTag.accountTag;
import static com.studyIn.domain.account.entity.QAuthentication.authentication;
import static com.studyIn.domain.account.entity.QProfile.profile;
import static com.studyIn.domain.tag.entity.QTag.tag;

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
    public List<String> findTagTitleByAccountId(Long id) {
        return jpaQueryFactory
                .select(tag.title)
                .from(account)
                .join(account.accountTags, accountTag)
                .join(accountTag.tag, tag)
                .where(account.id.eq(id))
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
}

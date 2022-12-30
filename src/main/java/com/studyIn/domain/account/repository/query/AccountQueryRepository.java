package com.studyIn.domain.account.repository.query;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.account.entity.QAuthentication.authentication;
import static com.studyIn.domain.account.entity.QProfile.profile;

@Repository
@RequiredArgsConstructor
public class AccountQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AccountDTO findCountAndNicknameByEmail(String email) {

        String nickname = queryFactory
                .select(profile.nickname.as("nickname"))
                .from(account)
                .join(account.authentication, authentication)
                .join(account.profile, profile)
                .where(authentication.email.eq(email))
                .fetchOne();

        Long totalCount = queryFactory
                .select(account.count())
                .from(account)
                .fetchOne();

        return new AccountDTO(totalCount, nickname);
    }
}

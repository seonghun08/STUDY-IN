package com.studyIn.domain.notification.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.notification.Notification;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.studyIn.domain.account.entity.QAccount.account;
import static com.studyIn.domain.notification.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public int countByAccountAndChecked(Long accountId, boolean checked) {
        Long count = jpaQueryFactory
                .select(notification.count())
                .from(notification)
                .join(notification.account, account)
                .where(account.id.eq(accountId).and(notification.checked.eq(checked)))
                .fetchOne();
        assert count != null;
        return count.intValue();
    }

    @Override
    public List<Notification> findNotificationsByChecked(Long accountId, boolean checked) {
        return jpaQueryFactory
                .selectFrom(notification)
                .join(notification.account, account).fetchJoin()
                .where(account.id.eq(accountId).and(notification.checked.eq(checked)))
                .orderBy(notification.createdDate.asc())
                .fetch();
    }
}

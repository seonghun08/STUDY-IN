package com.studyIn.domain.notification.repository;

import com.studyIn.domain.notification.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {

    int countByAccountAndChecked(Long accountId, boolean checked);

    List<Notification> findNotificationsByChecked(Long accountId, boolean checked);

    Long deleteByAccountAndChecked(Long accountId, boolean checked);
}

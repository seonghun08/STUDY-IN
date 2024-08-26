package com.studyIn.modules.notification;

import com.studyIn.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public int findNotificationsCountByChecked(Account account, boolean checked) {
        return (int) notificationRepository.countByAccountAndChecked(account, checked);
    }

    public List<Notification> findNotificationsByChecked(Account account, boolean checked) {
        return notificationRepository.findByAccountAndCheckedOrderByCreatedDateTime(account, checked);
    }

    public void readChecked(List<Notification> notificationList) {
        notificationList.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(notificationList);
    }

    public void deleteNotifications(Account account, boolean checked) {
        notificationRepository.deleteByAccountAndChecked(account, checked);
    }
}

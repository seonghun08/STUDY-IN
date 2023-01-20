package com.studyIn.domain.notification;

import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;

    public void readChecked(List<Notification> notificationList) {
        notificationList.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(notificationList);
    }

//    public void deleteNotifications(AccountInfo accountInfo, boolean checked) {
//        Account account = accountRepository.findById(accountInfo.getAccountId()).orElseThrow(IllegalArgumentException::new);
//        notificationRepository.deleteByAccountAndChecked(account, checked);
//    }
}

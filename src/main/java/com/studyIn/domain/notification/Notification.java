package com.studyIn.domain.notification;

import com.studyIn.domain.account.entity.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter @Setter
@NoArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "notification_id")
    private Long id;

    private String title;
    private String link;
    private String message;
    private boolean checked;

    @ManyToOne
    @JoinColumn(name = "account_Id")
    private Account account;

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
}

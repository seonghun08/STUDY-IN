package com.studyIn.domain.event;

import com.studyIn.domain.account.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_enrollment")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime enrolledDate;

    private boolean accepted;
    private boolean attended;

    /**
     * Service method
     */
    public boolean checkIn() {
        if (!this.accepted) {
            return false;

        } else {
            this.attended = true;
            return true;
        }

    }

    public boolean cancelCheckIn() {
        if (!this.accepted && !this.attended) {
            return false;

        } else {
            this.attended = false;
            return true;
        }

    }
}

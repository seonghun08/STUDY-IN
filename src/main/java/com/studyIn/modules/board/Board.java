package com.studyIn.modules.board;

import com.studyIn.modules.account.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Board {

    @Id @GeneratedValue
    private Long id;

    private Long seq;

    @ManyToOne
    private Account writer;

    private String title;

    @Lob @Basic(fetch = LAZY)
    private String content;

    private LocalDateTime dateCreated;

    private int views;

    public void writerRegistering(Account account) {
        this.writer = account;
        account.getBoards().add(this);
    }

    public void increaseInViews() {
        this.views++;
    }
}

package com.studyIn.modules.event;

import com.studyIn.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Enrollment.withEventAndStudy",
        attributeNodes = {@NamedAttributeNode(value = "event", subgraph = "study")},
        subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

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
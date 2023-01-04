package com.studyIn.domain.study.entity;

import com.studyIn.domain.account.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "study_manager")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyManager {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_manager_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
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


    //== 생성 메서드 ==//
    public static StudyManager createStudyManager(Account account) {
        StudyManager studyManager = new StudyManager();
        studyManager.account = account;
        return studyManager;
    }
}
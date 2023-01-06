package com.studyIn.domain.study.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.study.dto.form.StudyForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "study")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    /* Account.id */
    @CreatedBy
    private Long createdBy;

    @Column(nullable = false, unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String bannerImage;
    private boolean useBanner;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyManager> managers = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyTag> studyTags = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyLocation> studyLocations = new ArrayList<>();

    /**
     * 스터디 모임 공개
     */
    private boolean published;
    private LocalDateTime publishedDateTime;

    /**
     * 인원 모집 여부
     */
    private boolean recruiting;
    private LocalDateTime recruitingUpdatedDateTime;

    /**
     * 스터디 모임 종료
     */
    private boolean closed;
    private LocalDateTime closedDateTime;


    //== 연관관계 메서드 ==//
    public void setStudyManager(StudyManager studyManager) {
        this.managers.add(studyManager);
        studyManager.setStudy(this);
    }

    public void setStudyMember(StudyMember studyMember) {
        this.members.add(studyMember);
        studyMember.setStudy(this);
    }


    //== 생성 메서드 ==//
    public static Study createStudy(StudyForm form, StudyManager studyManager) {
        Study study = new Study();
        study.path = form.getPath();
        study.title = form.getTitle();
        study.shortDescription = form.getShortDescription();
        study.fullDescription = form.getFullDescription();
        study.setStudyManager(studyManager);
        return study;
    }
}

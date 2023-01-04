package com.studyIn.domain.study.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String path;

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


    //== 생성 메서드 ==//
    public static Study createStudy() {
        Study study = new Study();
        return study;
    }
}

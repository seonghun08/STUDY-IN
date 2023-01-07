package com.studyIn.domain.study.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.dto.form.PathForm;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.dto.form.TitleForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    /* CreatedBy Account.id */
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
    private LocalDateTime publishedDate;

    /**
     * 인원 모집 여부
     */
    private boolean recruiting;
    private LocalDateTime recruitingUpdatedDate;

    /**
     * 스터디 모임 종료
     */
    private boolean closed;
    private LocalDateTime closedDate;


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


    //== 수정 메서드 ==//
    public void updateDescription(DescriptionForm descriptionForm) {
        this.shortDescription = descriptionForm.getShortDescription();
        this.fullDescription = descriptionForm.getFullDescription();
    }

    public void updateBannerImage(String image) {
        this.bannerImage = image;
    }

    public void updateUseBanner(boolean check) {
        this.useBanner = check;
    }

    public void addStudyTag(StudyTag studyTag) {
        this.studyTags.add(studyTag);
        studyTag.setStudy(this);
    }

    public void addStudyLocation(StudyLocation studyLocation) {
        this.studyLocations.add(studyLocation);
        studyLocation.setStudy(this);
    }

    public void updatePath(PathForm pathForm) {
        this.path = pathForm.getPath();
    }

    public void updateTitle(TitleForm titleForm) {
        this.title = titleForm.getTitle();
    }

    public void publish() {
        if (!this.published && !this.recruiting) {
            this.published = true;
            this.publishedDate = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디 공개를 할 수 없는 상태입니다. 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDate = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료 할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDate = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 30분 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDate = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 30분 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && (this.recruitingUpdatedDate == null) || this.recruitingUpdatedDate.isBefore(LocalDateTime.now().minusMinutes(30));
    }

}

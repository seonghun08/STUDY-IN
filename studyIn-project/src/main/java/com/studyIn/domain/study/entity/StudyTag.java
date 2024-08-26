package com.studyIn.domain.study.entity;

import com.studyIn.domain.tag.Tag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "study_tag")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    //== 생성 메서드 ==//
    public static StudyTag createStudyTag(Tag tag) {
        StudyTag studyTag = new StudyTag();
        studyTag.setTag(tag);
        return studyTag;
    }
}
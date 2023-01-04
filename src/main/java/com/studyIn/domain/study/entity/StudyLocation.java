package com.studyIn.domain.study.entity;

import com.studyIn.domain.location.Location;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "study_location")
@Getter @Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyLocation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_location_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    //== 생성 메서드 ==//
    public static StudyLocation createStudyLocation(Location location) {
        StudyLocation studyLocation = new StudyLocation();
        studyLocation.setLocation(location);
        return studyLocation;
    }
}
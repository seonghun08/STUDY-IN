package com.studyIn.domain.tag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tag")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    //== 생성 메서드 ==//
    public static Tag createTag(String title) {
        Tag tag = new Tag();
        tag.title = title;
        return tag;
    }
}

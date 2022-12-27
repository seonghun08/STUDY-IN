package com.studyIn.domain.account.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_profile")
public class Profile {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private Long id;

    private String nickname;

    public Profile(String nickname) {
        this.nickname = nickname;
    }

    private String bio;

    private String url;

    private String occupation;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String image;
}

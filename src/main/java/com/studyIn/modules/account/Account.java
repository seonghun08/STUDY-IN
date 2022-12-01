package com.studyIn.modules.account;

import com.studyIn.modules.board.Board;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.tag.Tag;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static javax.persistence.FetchType.EAGER;

@NamedEntityGraph(
        name = "Account.withAll",
        attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("locations")
        })

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
//@NoArgsConstructor: [parameter]가 없는 기본 생성자 생성
//@AllArgsConstructor: 모든 필드 값을 [parameter]로 받는 생성자 생성
@AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column
    private String nickname;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private GenderType genderType;

    private int age;

    private String password;

    private boolean emailVerified;

    private boolean emailCheckConfirm;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String url;

    private String bio;

    private String occupation;

    private String address;

    private String phone;

    private String birthday;

    @Lob @Basic(fetch = EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Location> locations = new HashSet<>();

    @OneToMany(mappedBy = "writer")
    private List<Board> boards = new ArrayList<>();


    /**
     * Service method
     */
    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void checkedEmail() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        if (!emailCheckConfirm) {
            this.emailCheckConfirm = true;
            return true;
        }
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(10));
    }

}

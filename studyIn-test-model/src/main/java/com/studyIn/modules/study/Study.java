package com.studyIn.modules.study;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.UserAccount;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.tag.Tag;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;

@NamedEntityGraph(
        name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("members"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("locations")}
)

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = LAZY)
    private String fullDescription;

    @Lob @Basic(fetch = LAZY)
    private String image;

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Location> locations = new HashSet<>();


    /**
     * Service method
     */
    public void addManager(Account account) {
        this.managers.add(account);
    }

    public void addMember(Account account) {
        if (this.managers.contains(account)) {
            throw new IllegalArgumentException("이미 가입된 회원입니다.");
        }
        this.members.add(account);
        this.memberCount++;
    }
    public void leaveMember(Account account) {
        if (!this.members.contains(account)) {
            throw new IllegalArgumentException("이미 가입되지 않은 회원입니다.");
        }
        this.members.remove(account);
        this.memberCount--;
    }

    public void publish() {
        if (!this.published && !this.recruiting) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }
    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }
    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && (this.recruitingUpdatedDateTime == null) || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }
    public boolean isRemovable() {
        return !this.published;
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isManagerBy(Account account) {
        return this.getManagers().contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.members.contains(account);
    }

    public boolean isManager(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.managers.contains(account);
    }

    public boolean isMemberOrManager(Account account) {
        return this.getMembers().contains(account) || this.getManagers().contains(account);
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.jpg";
    }
}
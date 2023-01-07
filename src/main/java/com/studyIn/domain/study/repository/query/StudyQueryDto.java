package com.studyIn.domain.study.repository.query;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.UserAccount;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class StudyQueryDto {

    private Long studyId;
    private String path;
    private String title;

    private String shortDescription;
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String bannerImage;
    private boolean useBanner;

    private List<StudyAccountDto> managers = new ArrayList<>();
    private List<StudyAccountDto> members = new ArrayList<>();

    private List<StudyTagDto> tags = new ArrayList<>();
    private List<StudyLocationDto> locations = new ArrayList<>();

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

    public StudyQueryDto(Long studyId, String path, String title, String shortDescription, String fullDescription,
                         boolean published, LocalDateTime publishedDate, boolean recruiting, LocalDateTime recruitingUpdatedDate,
                         boolean closed, LocalDateTime closedDate, String bannerImage, boolean useBanner) {
        this.studyId = studyId;
        this.path = path;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.published = published;
        this.publishedDate = publishedDate;
        this.recruiting = recruiting;
        this.recruitingUpdatedDate = recruitingUpdatedDate;
        this.closed = closed;
        this.closedDate = closedDate;
        this.bannerImage = bannerImage;
        this.useBanner = useBanner;
    }

    public boolean isJoinAble(UserAccount userAccount) {
        String username = userAccount.getAccountInfo().getUsername();
        return this.isPublished()
                && this.isRecruiting()
                && !isContainsMember(username)
                && !isContainsManager(username);
    }

    public boolean isMember(UserAccount userAccount) {
        String username = userAccount.getAccountInfo().getUsername();
        return isContainsMember(username);
    }
    public boolean isManager(UserAccount userAccount) {
        String username = userAccount.getAccountInfo().getUsername();
        return isContainsManager(username);
    }

    public boolean isManagerBy(AccountInfo accountInfo) {
        return isContainsManager(accountInfo.getUsername());
    }

    private boolean isContainsManager(String username) {
        return managers.stream()
                .map(m -> m.getUsername())
                .collect(Collectors.toList())
                .contains(username);
    }
    private boolean isContainsMember(String username) {
        return members.stream()
                .map(m -> m.getUsername())
                .collect(Collectors.toList())
                .contains(username);
    }

    public String getBannerImage() {
        return this.bannerImage != null ? bannerImage : "/images/default_banner.jpg";
    }

    public boolean isRemovable() {
        return !this.published;
    }
}

package com.studyIn.domain.study.dto;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.UserAccount;
import com.studyIn.domain.account.dto.AccountDto;
import com.studyIn.domain.study.entity.Study;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class StudyDto {

    private Long studyId;
    private String path;
    private String title;

    private String shortDescription;
    private String fullDescription;

    private boolean published;
    private boolean recruiting;
    private boolean closed;

    @Lob @Basic(fetch = FetchType.LAZY)
    private String bannerImage;
    private boolean useBanner;

    private List<AccountDto> managers;
    private List<AccountDto> members;

    private List<String> tagTitles;
    private List<String> locationTitles;

    public StudyDto(Study study) {
        this.studyId = study.getId();
        this.path = study.getPath();
        this.title = study.getTitle();
        this.shortDescription = study.getShortDescription();
        this.fullDescription = study.getFullDescription();
        this.published = study.isPublished();
        this.recruiting = study.isRecruiting();
        this.closed = study.isClosed();
        this.bannerImage = study.getBannerImage();
        this.useBanner = study.isUseBanner();

        this.managers = study.getManagers().stream()
                .map(m -> new AccountDto(m.getAccount()))
                .collect(Collectors.toList());
        this.members = study.getMembers().stream()
                .map(m -> new AccountDto(m.getAccount()))
                .collect(Collectors.toList());

        this.tagTitles = study.getStudyTags().stream()
                .map(s -> s.getTag().getTitle())
                .collect(Collectors.toList());
        this.locationTitles = study.getStudyLocations().stream()
                .map(l -> l.getLocation().getLocalNameOfCity())
                .collect(Collectors.toList());
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
}

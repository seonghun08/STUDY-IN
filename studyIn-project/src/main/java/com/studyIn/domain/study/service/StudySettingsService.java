package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.dto.form.PathForm;
import com.studyIn.domain.study.dto.form.TitleForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyLocation;
import com.studyIn.domain.study.entity.StudyTag;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.dto.StudyQueryDto;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StudySettingsService {

    private final StudyRepository studyRepository;
    private final StudyQueryRepository studyQueryRepository;

    /**
     * 스터디 조회 및 업데이트 권한 확인
     */
    public StudyQueryDto getStudyPermissionToUpdate(AccountInfo accountInfo, String path) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        checkPermissions(accountInfo, study);
        return study;
    }

    /**
     * 소개글 수정
     */
    public void updateDescription(AccountInfo accountInfo, DescriptionForm descriptionForm, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateDescription(descriptionForm);
                });
    }

    /**
     * 배너 이미지 수정
     */
    public void updateBanner(AccountInfo accountInfo, String image, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateBannerImage(image);
                });
    }

    /**
     * 배너 활성화
     */
    public void enableBanner(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateUseBanner(true);
                });
    }

    /**
     * 배너 비활성화
     */
    public void disableBanner(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateUseBanner(false);
                });
    }

    /**
     * 주제 태그 수정 (추가)
     */
    public void addTag(AccountInfo accountInfo, Tag tag, String path) {
        StudyTag studyTag = StudyTag.createStudyTag(tag);
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.addStudyTag(studyTag);
                });
    }

    /**
     * 주제 태그 수정 (삭제)
     */
    public void removeTag(AccountInfo accountInfo, Tag tag, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    studyRepository.deleteStudyTag(tag.getId(), study.getId());
                });
    }

    /**
     * 지역 태그 수정 (추가)
     */
    public void addLocation(AccountInfo accountInfo, Location location, String path) {
        StudyLocation studyLocation = StudyLocation.createStudyLocation(location);
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.addStudyLocation(studyLocation);
                });
    }

    /**
     * 지역 태그 수정 (삭제)
     */
    public void removeLocation(AccountInfo accountInfo, Location location, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    studyRepository.deleteStudyLocation(location.getId(), study.getId());
                });
    }

    /**
     * 스터디 공개
     */
    public void publish(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.publish();
                });
    }

    /**
     * 스터디 종료
     */
    public void close(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.close();
                });
    }

    /**
     * 스터디 인원 모집 시작
     */
    public void startRecruit(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.startRecruit();
                });
    }

    /**
     * 스터디 인원 모집 종료
     */
    public void stopRecruit(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.stopRecruit();
                });
    }

    /**
     * 스터디 경로 변경
     */
    public String updatePath(AccountInfo accountInfo, PathForm pathForm, String path) {
        Study study = studyRepository.findWithManagersByPath(path)
                .orElseThrow(IllegalArgumentException::new);
        checkPermissions(accountInfo, study);
        study.updatePath(pathForm);
        return study.getPath();
    }

    /**
     * 스터디 이름 변경
     */
    public void updateTitle(AccountInfo accountInfo, TitleForm titleForm, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateTitle(titleForm);
                });
    }

    /**
     * 스터디 삭제
     */
    public void removeStudy(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    if (!study.isPublished()) {
                        studyRepository.delete(study);
                    } else {
                        throw new RuntimeException("스터디를 삭제할 수 없습니다.");
                    }
                });
    }

    private void checkPermissions(AccountInfo accountInfo, Study study) {
        if (!study.getManagers().stream()
                .map(m -> m.getAccount().getUsername())
                .collect(Collectors.toList())
                .contains(accountInfo.getUsername())) {
            throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
        }
    }

    private void checkPermissions(AccountInfo accountInfo, StudyQueryDto studyDto) {
        if (!studyDto.isManagerBy(accountInfo)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
        }
    }

}

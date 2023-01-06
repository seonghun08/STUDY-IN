package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.repository.query.StudyQueryDto;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
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

    public StudyQueryDto getStudyPermissionToUpdate(AccountInfo accountInfo, String path) {
        StudyQueryDto study = studyQueryRepository.findStudyQueryDtoByPath(path);
        checkPermissions(accountInfo, study);
        return study;
    }

    public void updateDescription(AccountInfo accountInfo, DescriptionForm descriptionForm, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateDescription(descriptionForm);
                });
    }

    public void updateBanner(AccountInfo accountInfo, String image, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateBannerImage(image);
                });
    }

    public void enableBanner(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateUseBanner(true);
                });
    }

    public void disableBanner(AccountInfo accountInfo, String path) {
        studyRepository.findWithManagersByPath(path)
                .ifPresent(study -> {
                    checkPermissions(accountInfo, study);
                    study.updateUseBanner(false);
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

package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.study.dto.StudyDto;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudySettingsService {

    private final StudyRepository studyRepository;

    public StudyDto getStudyPermissionToUpdate(AccountInfo accountInfo, String path) {
        Study study = studyRepository.findByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + "해당하는 스터디가 존재하지 않습니다."));
        StudyDto studyDto = new StudyDto(study);
        checkPermissions(accountInfo, studyDto);
        return studyDto;
    }

    private void checkPermissions(AccountInfo accountInfo, StudyDto studyDto) {
        if (!studyDto.isManagerBy(accountInfo)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 있는 권한이 없습니다.");
        }
    }
}

package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.study.dto.StudyDto;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyManager;
import com.studyIn.domain.study.entity.StudyMember;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;

    public Study createStudy(AccountInfo accountInfo, StudyForm form) {
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);
        StudyManager studyManager = StudyManager.createStudyManager(account);
        Study study = Study.createStudy(form, studyManager);
        return studyRepository.save(study);
    }

    public StudyDto getStudyDtoByPath(String path) {
        Study study = studyRepository.findByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + "해당하는 스터디가 존재하지 않습니다."));
        return new StudyDto(study);
    }
}

package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyManager;
import com.studyIn.domain.study.entity.StudyMember;
import com.studyIn.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 스터디 생성
     */
    public Study createStudy(AccountInfo accountInfo, StudyForm form) {
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);
        StudyManager studyManager = StudyManager.createStudyManager(account);
        Study study = Study.createStudy(form, studyManager);
        return studyRepository.save(study);
    }

    /**
     * 스터디 회원 등록
     */
    public void addMember(AccountInfo accountInfo, String path) {
        Account account = accountRepository.findById(accountInfo.getAccountId())
                .orElseThrow(IllegalArgumentException::new);
        StudyMember studyMember = StudyMember.createStudyMember(account);
        studyRepository.findWithMembersByPath(path)
                .ifPresent(study -> {
                    if (study.isPublished() && study.isRecruiting()) study.setStudyMember(studyMember);
                });
    }

    /**
     * 스터디 회원 탈퇴
     */
    public void leaveMember(AccountInfo accountInfo, String path) {
        studyRepository.findByPath(path)
                .ifPresent(study -> studyRepository.deleteStudyMember(accountInfo.getAccountId(), study.getId()));
    }

    public void checkPermissions(String studyPath, Long accountId) {
        if (!studyRepository.existStudyManagerByStudyPathAndAccountId(studyPath, accountId)) {
            throw new AccessDeniedException("해당 기능의 권한이 없습니다.");
        }
    }
}

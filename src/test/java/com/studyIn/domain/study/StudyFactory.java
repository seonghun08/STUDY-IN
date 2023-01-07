package com.studyIn.domain.study;

import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyManager;
import com.studyIn.domain.study.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {

    @Autowired StudyRepository studyRepository;

    public Study createStudy(String path, Account account) {
        StudyForm form = createStudyForm(path);
        StudyManager studyManager = StudyManager.createStudyManager(account);
        Study study = Study.createStudy(form, studyManager);
        return studyRepository.save(study);
    }

    private StudyForm createStudyForm(String path) {
        StudyForm studyForm = new StudyForm();
        studyForm.setPath(path);
        studyForm.setTitle("study-" + path);
        studyForm.setShortDescription("short description");
        studyForm.setFullDescription("full description");
        return studyForm;
    }
}

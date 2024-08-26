package com.studyIn.modules.study;

import com.studyIn.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

    @Autowired StudyRepository studyRepository;

    public Study createStudy(String path, Account account) {
        Study study = new Study();
        study.addManager(account);
        study.setPath(path);
        study.setTitle("studyTitle");
        return studyRepository.save(study);
    }
}

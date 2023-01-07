package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.study.dto.form.StudyForm;
import com.studyIn.domain.study.repository.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class StudyServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountFactory accountFactory;

    @Test
    void createStudy() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        AccountInfo accountInfo = new AccountInfo(account);

        StudyForm studyForm = new StudyForm();
        studyForm.setTitle("study-title");
        studyForm.setPath("study-path");
        studyForm.setShortDescription("short description");
        studyForm.setFullDescription("full description");

        //when
        studyService.createStudy(accountInfo, studyForm);
        em.flush();
        em.clear();

        //then
        assertThat(studyRepository.existsByPath(studyForm.getPath())).isTrue();
    }
}
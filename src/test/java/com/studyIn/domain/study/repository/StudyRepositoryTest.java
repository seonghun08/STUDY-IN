package com.studyIn.domain.study.repository;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.entity.Study;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class StudyRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Test
    void existsByTitle() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        assertThat(studyRepository.existsByTitle(study.getTitle())).isTrue();
    }

    @Test
    void existsByPath() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        assertThat(studyRepository.existsByPath(study.getPath())).isTrue();
    }

    @Test
    void findByPath() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        Study findStudy = studyRepository.findByPath(study.getPath())
                .orElseThrow(RuntimeException::new);
        assertThat(study.getId()).isEqualTo(findStudy.getId());
    }
}
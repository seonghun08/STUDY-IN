package com.studyIn.domain.study.repository.query;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.dto.StudyQueryDto;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.tag.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class StudyQueryRepositoryTest {

    @PersistenceContext EntityManager em;
    @Autowired StudyQueryRepository studyQueryRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;

    @Test
    void findStudyQueryDtoByPath() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        StudyQueryDto dto = studyQueryRepository.findStudyQueryDtoByPath(study.getPath());
        assertThat(dto.getStudyId()).isEqualTo(study.getId());
        assertThat(dto.getPath()).isEqualTo(study.getPath());
        assertThat(dto.getTitle()).isEqualTo(study.getTitle());
        assertThat(dto.getShortDescription()).isEqualTo(study.getShortDescription());
        assertThat(dto.getFullDescription()).isEqualTo(study.getFullDescription());

        List<String> managers = dto.getManagers().stream()
                .map(m -> m.getUsername())
                .collect(Collectors.toList());
        assertThat(managers.contains(account.getUsername())).isTrue();
    }
}
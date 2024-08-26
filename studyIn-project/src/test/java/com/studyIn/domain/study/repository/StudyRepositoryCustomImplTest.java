package com.studyIn.domain.study.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyLocation;
import com.studyIn.domain.study.entity.StudyManager;
import com.studyIn.domain.study.entity.StudyTag;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class StudyRepositoryCustomImplTest {

    @PersistenceContext EntityManager em;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;

    private Tag createTag(String title) {
        return tagRepository.save(Tag.createTag(title));
    }
    private Location createLocation() {
        return locationRepository.save(Location.createLocation("testCity", "테스트도시", "none"));
    }

    @Test
    void findWithManagersByPath() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when
        Study findStudy = studyRepository.findWithManagersByPath(study.getPath())
                .orElseThrow(RuntimeException::new);
        /**
         * LAZY 로딩이 일어나면 안된다.
         */
        findStudy.getManagers().stream()
                .map(StudyManager::getAccount)
                .collect(Collectors.toList())
                .forEach(a -> System.out.println(a.getUsername()));
    }

    @Test
    void deleteStudyTag() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);

        Tag tag = createTag("spring boot");
        StudyTag studyTag = StudyTag.createStudyTag(tag);
        study.addStudyTag(studyTag);
        em.flush();
        em.clear();

        //when, then
        Long count = studyRepository.deleteStudyTag(tag.getId(), study.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteStudyLocation() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Study study = studyFactory.createStudy("path", account);

        Location location = createLocation();
        StudyLocation studyLocation = StudyLocation.createStudyLocation(location);
        study.addStudyLocation(studyLocation);
        em.flush();
        em.clear();

        //when, then
        Long count = studyRepository.deleteStudyLocation(location.getId(), study.getId());
        assertThat(count).isEqualTo(1);
    }
}
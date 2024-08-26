package com.studyIn.domain.study.service;

import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.study.StudyFactory;
import com.studyIn.domain.study.dto.StudyQueryDto;
import com.studyIn.domain.study.dto.form.DescriptionForm;
import com.studyIn.domain.study.entity.Study;
import com.studyIn.domain.study.entity.StudyTag;
import com.studyIn.domain.study.repository.StudyRepository;
import com.studyIn.domain.study.repository.query.StudyQueryRepository;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class StudySettingsServiceTest {

    @PersistenceContext EntityManager em;
    @Autowired StudySettingsService studySettingsService;
    @Autowired StudyRepository studyRepository;
    @Autowired StudyQueryRepository studyQueryRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;

    @Test
    void getStudyPermissionToUpdate() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        StudyQueryDto dto = studySettingsService.getStudyPermissionToUpdate(accountInfo, study.getPath());
        assertThat(dto.getTitle()).isEqualTo(study.getTitle());
    }

    @Test
    void updateDescription() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        //when
        String shortDescription = "hello";
        String fullDescription = "full description";
        DescriptionForm descriptionForm = new DescriptionForm();
        descriptionForm.setShortDescription(shortDescription);
        descriptionForm.setFullDescription(fullDescription);
        studySettingsService.updateDescription(accountInfo, descriptionForm, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        assertThat(findStudy.getShortDescription()).isEqualTo(shortDescription);
        assertThat(findStudy.getFullDescription()).isEqualTo(fullDescription);
    }

    @Test
    void updateBanner() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        //when
        String image = "abcdefg";
        studySettingsService.updateBanner(accountInfo, image, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        assertThat(findStudy.getBannerImage()).isEqualTo(image);
    }

    @Test
    void enableBanner() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        studySettingsService.enableBanner(accountInfo, study.getPath());
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        assertThat(findStudy.isUseBanner()).isTrue();
    }

    @Test
    void disableBanner() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);
        em.flush();
        em.clear();

        //when, then
        studySettingsService.disableBanner(accountInfo, study.getPath());
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        assertThat(findStudy.isUseBanner()).isFalse();
    }

    @Test
    void addTag() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        //when
        Tag tag = Tag.createTag("spring");
        tagRepository.save(tag);
        studySettingsService.addTag(accountInfo, tag, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        List<String> tagTitles = findStudy.getStudyTags().stream()
                .map(s -> s.getTag().getTitle())
                .collect(Collectors.toList());
        assertThat(tagTitles.contains(tag.getTitle())).isTrue();
    }

    @Test
    void removeTag() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        Tag tag = Tag.createTag("spring");
        tagRepository.save(tag);
        studySettingsService.addTag(accountInfo, tag, study.getPath());

        //when
        studySettingsService.removeTag(accountInfo, tag, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        List<Tag> Tags = findStudy.getStudyTags().stream()
                .map(s -> s.getTag())
                .collect(Collectors.toList());
        assertThat(Tags).isEmpty();
    }

    @Test
    void addLocation() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        //when
        Location location = Location.createLocation("test", "테스트시티", "none");
        locationRepository.save(location);
        studySettingsService.addLocation(accountInfo, location, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        List<String> locationLocalNameOfCity = findStudy.getStudyLocations().stream()
                .map(s -> s.getLocation().getLocalNameOfCity())
                .collect(Collectors.toList());
        assertThat(locationLocalNameOfCity.contains(location.getLocalNameOfCity())).isTrue();
    }

    @Test
    void removeLocation() {
        //given
        Account account = accountFactory.createAccount("user");
        AccountInfo accountInfo = new AccountInfo(account);
        Study study = studyFactory.createStudy("path", account);

        Location location = Location.createLocation("test", "테스트시티", "none");
        locationRepository.save(location);
        studySettingsService.addLocation(accountInfo, location, study.getPath());

        //when
        studySettingsService.removeLocation(accountInfo, location, study.getPath());
        em.flush();
        em.clear();

        //then
        Study findStudy = studyRepository.findById(study.getId())
                .orElseThrow(RuntimeException::new);
        List<Location> locations = findStudy.getStudyLocations().stream()
                .map(s -> s.getLocation())
                .collect(Collectors.toList());
        assertThat(locations).isEmpty();
    }
}
package com.studyIn.domain.account.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.AccountFactory;
import com.studyIn.domain.account.dto.ProfileDto;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.studyIn.domain.account.entity.QAccountTag.accountTag;
import static com.studyIn.domain.tag.QTag.tag;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AccountRepositoryCustomImplTest {

    @PersistenceContext EntityManager em;
    @Autowired AccountRepository accountRepository;
    @Autowired TagRepository tagRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired PasswordEncoder passwordEncoder;

    private Tag createTag(String title) {
        return tagRepository.save(Tag.createTag(title));
    }
    private Location createLocation() {
        return locationRepository.save(Location.createLocation("testCity", "테스트도시", "none"));
    }

    @Test
    void findProfileDtoByUsername() throws Exception {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        em.flush();
        em.clear();

        //when, then
        ProfileDto dto = accountRepository.findProfileDtoByUsername(account.getUsername())
                .orElseThrow(IllegalArgumentException::new);
        assertThat(dto.getUsername()).isEqualTo(account.getUsername());
        assertThat(dto.getEmail()).isEqualTo(account.getAuthentication().getEmail());
    }

    @Test
    void findTagsByAccountId() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Tag tag = createTag("spring");
        AccountTag accountTag = AccountTag.createAccountTag(tag);
        account.addAccountTag(accountTag);
        em.flush();
        em.clear();

        //when, then
        Tag findTag = accountRepository.findTagsByAccountId(account.getId()).get(0);
        assertThat(findTag.getTitle()).isEqualTo(tag.getTitle());
    }

    @Test
    void findLocationsById() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Location location = createLocation();
        AccountLocation accountLocation = AccountLocation.createAccountLocation(location);
        account.addAccountLocation(accountLocation);
        em.flush();
        em.clear();

        //when, then
        Location findLocation = accountRepository.findLocationsById(account.getId()).get(0);
        assertThat(findLocation.toString()).isEqualTo(location.toString());
    }

    @Test
    void findPasswordByUsername() {
        //given
        SignUpForm form = accountFactory.createSignUpForm("spring-dev");
        form.setPassword("123123123");
        accountFactory.createAccount(form);
        em.flush();
        em.clear();

        //when
        String findPassword = accountRepository.findPasswordByUsername(form.getUsername());
        assertThat(passwordEncoder.matches(form.getPassword(), findPassword)).isTrue();
    }

    @Test
    void deleteAccountTag() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Tag tag = createTag("spring");
        AccountTag accountTag = AccountTag.createAccountTag(tag);
        account.addAccountTag(accountTag);

        //when
        Long count = accountRepository.deleteAccountTag(tag.getId(), account.getId());
        em.flush();
        em.clear();

        //then
        List<Tag> findTags = accountRepository.findTagsByAccountId(account.getId());
        assertThat(findTags).isEmpty();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteAccountLocation() {
        //given
        Account account = accountFactory.createAccount("spring-dev");
        Location location = createLocation();
        AccountLocation accountLocation = AccountLocation.createAccountLocation(location);
        account.addAccountLocation(accountLocation);

        //when
        Long count = accountRepository.deleteAccountLocation(location.getId(), account.getId());
        em.flush();
        em.clear();

        //when, then
        List<Location> findLocations = accountRepository.findLocationsById(account.getId());
        assertThat(findLocations).isEmpty();
        assertThat(count).isEqualTo(1);
    }
}
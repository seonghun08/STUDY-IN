package com.studyIn.domain.account.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyIn.domain.account.entity.*;
import com.studyIn.domain.account.entity.value.Gender;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.dto.ProfileDto;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.tag.QTag;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
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
    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired TagRepository tagRepository;

    private Account createAccount(SignUpForm form) {
        NotificationsSetting notificationsSetting = new NotificationsSetting();
        Authentication authentication = Authentication.createAuthentication(form, notificationsSetting);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);

        /* 임시 토근 생성 */
        account.getAuthentication().generateEmailToken();
        return accountRepository.save(account);
    }

    private SignUpForm createSignUpForm(String username) {
        SignUpForm form = new SignUpForm();
        form.setUsername(username);
        form.setEmail(username + "@email.com");
        form.setPassword("1234567890");
        form.setNickname("nick-" + username);
        form.setCellPhone("01012341234");
        form.setGender(Gender.MAN);
        form.setBirthday("1997-08-30");
        return form;
    }

    @Test
    void findProfileDtoByUsername() throws Exception {
        //given
        SignUpForm form = createSignUpForm("spring-dev");
        Account account = createAccount(form);
        em.flush();
        em.clear();

        //when
        ProfileDto dto = accountRepository.findProfileDtoByUsername(form.getUsername()).orElseThrow();

        //then
        assertThat(dto.getUsername()).isEqualTo(account.getUsername());
        assertThat(dto.getEmail()).isEqualTo(account.getAuthentication().getEmail());
    }

    @Test
    void findAccountTagByAccountId() {
        SignUpForm form = createSignUpForm("spring-dev");
        Account account = createAccount(form);

        Tag tag = Tag.createTag("spring boot");
        tagRepository.save(tag);

        AccountTag accountTag = AccountTag.createAccountTag(tag);

        account.addAccountTag(accountTag);

        account.getAccountTags().forEach(a -> a.getTag());
        em.flush();
        em.clear();

        List<AccountTag> accountTags = em.createQuery(
                        "select at from AccountTag at" +
                                " join fetch at.account a" +
                                " join fetch at.tag t" +
                                " where a.username = :username", AccountTag.class)
                .setParameter("username", "spring-dev")
                .getResultList();

        accountTags.forEach(a -> {
            String title = a.getTag().getTitle();
            System.out.println("title = " + title);
        });

        List<String> titles = em.createQuery(
                        "select t.title from Account a" +
                                " join a.accountTags at" +
                                " join at.tag t" +
                                " where a.username = :username", String.class)
                .setParameter("username", "spring-dev")
                .getResultList();

        titles.forEach(t -> {
            System.out.println("t = " + t);
        });
    }

    @Test
    void exTest() {
        SignUpForm form = createSignUpForm("spring-dev");
        Account account = createAccount(form);

        Tag t = Tag.createTag("spring boot");
        tagRepository.save(t);

        AccountTag at = AccountTag.createAccountTag(t);
        account.addAccountTag(at);
        account.getAccountTags().forEach(a -> a.getTag());
        em.flush();
        em.clear();


        AccountTag findAccountTag = jpaQueryFactory
                .select(accountTag)
                .from(accountTag)
                .join(accountTag.tag, tag)
                .where(tag.title.eq("spring boot"))
                .fetchOne();

        System.out.println(findAccountTag.getTag().getTitle());
    }
}
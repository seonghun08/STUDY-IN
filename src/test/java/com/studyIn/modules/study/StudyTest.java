package com.studyIn.modules.study;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StudyTest {

    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        account = new Account();
        account.setNickname("nickname");
        account.setUsername("user");
        account.setEmail("user@email.com");
        account.setPassword("1234567890");
        userAccount = new UserAccount(account);
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이고, 현재 회원이나 관리자가 아니라면 스터디 가입 가능")
    @Test
    void isJoinAble() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이고, 이미 관리자일 경우 x")
    @Test
    void isJoinAble_false_for_manager() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이고, 이미 회원일 경우 x")
    @Test
    void isJoinAble_false_for_member() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.getMembers().add(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 관리자인지 확인")
    @Test
    void isManager() {
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }

    @DisplayName("스터디 멤버인지 확인")
    @Test
    void isMember() {
        study.getMembers().add(account);
        assertTrue(study.isMember(userAccount));
    }
}

package com.studyIn.domain.study.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StudyRepositoryCustomImplTest {

    @Autowired
    JPAQueryFactory jpaQueryFactory;


}
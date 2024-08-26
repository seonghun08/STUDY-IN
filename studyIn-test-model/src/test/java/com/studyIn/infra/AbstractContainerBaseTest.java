package com.studyIn.infra;

import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractContainerBaseTest {

    @ClassRule
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer("postgres:11.1");
        POSTGRE_SQL_CONTAINER.start();
    }
}
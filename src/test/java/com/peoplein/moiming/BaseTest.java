package com.peoplein.moiming;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@SpringBootTest
public class BaseTest {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public static GenericContainer container;
    @BeforeAll
    public static void setUp() {

        container = new MySQLContainer("mysql:8.0.31")
                .withDatabaseName("test")
                .withUsername("testuser")
                .withPassword("testpass")
                .withExposedPorts(3306);

        container.start();

        Integer mappedPort = container.getFirstMappedPort();
        System.setProperty("MYSQL_PORT", mappedPort.toString());
    }

    @BeforeEach
    void truncateTable() {
        TestUtils.truncateAllTable(jdbcTemplate);
    }

}

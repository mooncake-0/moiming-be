package com.peoplein.moiming;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@SpringBootTest
public class BaseTest {

    @BeforeAll
    public static void setUp() {

        GenericContainer genericContainer = new MySQLContainer("mysql:8.0.31")
                .withDatabaseName("test")
                .withUsername("testuser")
                .withPassword("testpass")
                .withExposedPorts(3306);

        genericContainer.start();

        Integer mappedPort = genericContainer.getFirstMappedPort();
        System.setProperty("MYSQL_PORT", mappedPort.toString());
    }


}

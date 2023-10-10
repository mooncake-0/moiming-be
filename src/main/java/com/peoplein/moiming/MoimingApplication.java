package com.peoplein.moiming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MoimingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoimingApplication.class, args);
    }
}

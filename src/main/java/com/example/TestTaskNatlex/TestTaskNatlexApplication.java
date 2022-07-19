package com.example.TestTaskNatlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestTaskNatlexApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestTaskNatlexApplication.class, args);
    }
}

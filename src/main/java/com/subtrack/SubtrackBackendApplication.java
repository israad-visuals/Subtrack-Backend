package com.subtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SubtrackBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubtrackBackendApplication.class, args);
    }

}

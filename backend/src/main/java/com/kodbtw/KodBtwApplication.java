package com.kodbtw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KodBtwApplication {

    public static void main(String[] args) {
        SpringApplication.run(KodBtwApplication.class, args);
    }
}

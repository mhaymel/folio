package com.folio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(FolioApplication.class, args);
    }
}
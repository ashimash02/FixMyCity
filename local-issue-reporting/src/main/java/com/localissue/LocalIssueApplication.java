package com.localissue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LocalIssueApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalIssueApplication.class, args);
    }
}

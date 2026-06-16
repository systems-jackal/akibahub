package com.akibahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Required for auto-rejecting expired proposals
public class AkibaHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(AkibaHubApplication.class, args);
        System.out.println("🚀 Akiba Hub V2 is running on http://localhost:8080");
    }
}
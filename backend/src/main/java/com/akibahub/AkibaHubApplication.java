package com.akibahub;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AkibaHubApplication {

    public static void main(String[] args) {
        // Load .env file from the current working directory
        Dotenv dotenv = Dotenv.load();

        // Set each property as a System property so Spring Boot picks them up
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        // If you have other variables like DB_DRIVER, add them too
        System.setProperty("SPRING_DATASOURCE_DRIVER_CLASS_NAME", 
            dotenv.get("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "org.mariadb.jdbc.Driver"));

        // Now start Spring Boot
        SpringApplication.run(AkibaHubApplication.class, args);
    }
}
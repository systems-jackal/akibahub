package com.akibahub;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Required for auto-rejecting expired proposals
public class AkibaHubApplication {

    public static void main(String[] args) {
        // Load .env file from the current working directory (backend/)
        Dotenv dotenv = Dotenv.load();

        // Inject all variables as System properties so Spring Boot sees them
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_DRIVER", dotenv.get("DB_DRIVER", "org.mariadb.jdbc.Driver"));
        System.setProperty("DB_DIALECT", dotenv.get("DB_DIALECT", "org.hibernate.dialect.MariaDBDialect"));

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION_MS", dotenv.get("JWT_EXPIRATION_MS", "86400000"));

        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID", ""));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET", ""));

        System.setProperty("PAYHERO_BASE_URL", dotenv.get("PAYHERO_BASE_URL", "https://api.sandbox.payhero.co.ke/v1"));
        System.setProperty("PAYHERO_API_KEY", dotenv.get("PAYHERO_API_KEY", ""));

        System.setProperty("CORS_ORIGINS", dotenv.get("CORS_ORIGINS", "http://localhost:5500,http://127.0.0.1:5500"));
        System.setProperty("FRONTEND_URL", dotenv.get("FRONTEND_URL", "http://localhost:5500"));

        // Start Spring Boot
        SpringApplication.run(AkibaHubApplication.class, args);
        System.out.println("🚀 Akiba Hub V2 is running on http://localhost:8080");
    }
}
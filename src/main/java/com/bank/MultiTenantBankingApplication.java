package com.bank;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MultiTenantBankingApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
		System.setProperty("SPRING_PROFILES_ACTIVE", dotenv.get("SPRING_PROFILES_ACTIVE"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("MAIL_PORT", dotenv.get("MAIL_PORT"));
		System.setProperty("MAIL_HOST", dotenv.get("MAIL_HOST"));
		System.setProperty("SUPPORT_EMAIL", dotenv.get("SUPPORT_EMAIL"));
		System.setProperty("APP_PASSWORD", dotenv.get("APP_PASSWORD"));
		System.setProperty("POSTGRES_DB_NAME", dotenv.get("POSTGRES_DB_NAME"));
		System.setProperty("POSTGRES_DB_USER", dotenv.get("POSTGRES_DB_USER"));
		System.setProperty("POSTGRES_DB_PASSWORD", dotenv.get("POSTGRES_DB_PASSWORD"));
		System.setProperty("POSTGRES_DB_PORT", dotenv.get("POSTGRES_DB_PORT"));
		System.setProperty("JWT_PRIVATE_KEY_PATH", dotenv.get("JWT_PRIVATE_KEY_PATH"));
		System.setProperty("JWT_PUBLIC_KEY_PATH", dotenv.get("JWT_PUBLIC_KEY_PATH"));
		System.setProperty("JWT_ACCESS_TOKEN_EXPIRATION", dotenv.get("JWT_ACCESS_TOKEN_EXPIRATION"));
		System.setProperty("JWT_REFRESH_TOKEN_EXPIRATION", dotenv.get("JWT_REFRESH_TOKEN_EXPIRATION"));

		SpringApplication.run(MultiTenantBankingApplication.class, args);
	}

}

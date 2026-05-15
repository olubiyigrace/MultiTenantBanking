package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MultiTenantBankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiTenantBankingApplication.class, args);
	}

}

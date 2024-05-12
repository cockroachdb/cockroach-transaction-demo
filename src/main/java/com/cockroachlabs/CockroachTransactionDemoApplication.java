package com.cockroachlabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot + Spring Data JPA sample application to demonstrate transactional operations.
 *
 * @author Greg L. Turnquist
 */
@SpringBootApplication
public class CockroachTransactionDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CockroachTransactionDemoApplication.class, args);
	}
}

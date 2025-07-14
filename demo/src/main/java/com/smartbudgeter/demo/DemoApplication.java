package com.smartbudgeter.demo;

import com.smartbudgeter.demo.models.Expense;
import com.smartbudgeter.demo.repositories.ExpenseRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		// Start the Spring application context
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

		System.out.println("✅ App running on port 8082...");

		
	}
}

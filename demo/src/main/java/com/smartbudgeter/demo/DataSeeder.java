package com.smartbudgeter.demo;

import com.smartbudgeter.demo.models.*;
import com.smartbudgeter.demo.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public DataSeeder(ExpenseRepository expenseRepo, UserRepository userRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed some initial users
        /*User user1 = new User();
        user1.setEmail("john_doe@mail.some");
        user1.setPassword("password123");
        user1.setDisplayName("John Doe");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setFamilyName("Doe");
        user1.setFamilyMembers(3);
        user1.setGoogleId("google-id-123");
        userRepo.save(user1);*/
        Expense e1 = new Expense();
        e1.setUserId(1);
        e1.setAmount(50.0f);
        e1.setCatId(2);
        e1.setDate(LocalDate.of(2025, 7, 14));
        e1.setCreatedAt(LocalDateTime.now());
        e1.setNote("Test expense 1");

        Expense e2 = new Expense();
        e2.setUserId(2);
        e2.setAmount(20.0f);
        e2.setCatId(1);
        e2.setDate(LocalDate.of(2025, 7, 13));
        e2.setCreatedAt(LocalDateTime.now());
        e2.setNote("Test expense 2");
        System.out.println("Seeding data...");
        expenseRepo.save(e1);
        expenseRepo.save(e2);
    }
}

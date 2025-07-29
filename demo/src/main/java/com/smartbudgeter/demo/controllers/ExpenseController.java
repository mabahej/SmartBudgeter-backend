package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController  {

    @Autowired
    private ExpenseRepository repository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        int userId = expense.getUser().getId();  // get ID from nested object
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        expense.setUser(user);
        return repository.save(expense);
    }

    @GetMapping
    public List<Expense> all() {
        return repository.findAll();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
   @GetMapping("/last-month-total/{userId}")
    public ResponseEntity<Float> getLastMonthTotal(@PathVariable int userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfLastMonth = today.minusMonths(0).withDayOfMonth(1); // minusMonths(1) for LAST month
            LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

            Float total = repository.getTotalExpensesForLastMonth(userId, firstDayOfLastMonth, lastDayOfLastMonth);
            System.out.println("Total expenses for last month"+ firstDayOfLastMonth+ " and "+ lastDayOfLastMonth+ ": " + total);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}

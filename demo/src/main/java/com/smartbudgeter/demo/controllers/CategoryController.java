package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @PostMapping
    public Category create(@RequestBody Category category) {
        int userId = category.getUser().getId();  // get ID from nested object
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        category.setUser(user);
        return repository.save(category);
    }
    

    @GetMapping
    public List<Category> all() {
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
    // Get all categories with their expenses summary (no DTO needed)
    @GetMapping("/summary/user/{userId}")
public List<Map<String, Object>> getUserCategoriesWithExpensesSummary(@PathVariable int userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Find categories for specific user
    List<Category> categories = repository.findByUserId(userId);
    
    return categories.stream().map(category -> {
        List<Expense> expenses = expenseRepository.findByCategoryIdAndUserId(
            category.getId(), userId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("categoryName", category.getName());
        summary.put("totalAmount", expenses.stream()
            .mapToDouble(expense -> expense.getAmount())
            .sum());
        summary.put("expenseCount", expenses.size());
        
        return summary;
    }).collect(Collectors.toList());
}
    
    // Get specific category with expenses
    @GetMapping("/{id}/expenses")
    public ResponseEntity<Map<String, Object>> getCategoryWithExpenses(@PathVariable int id) {
        Category category = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
            
        List<Expense> expenses = expenseRepository.findByCategoryId((int) id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("category", category);
        result.put("expenses", expenses);
        result.put("totalAmount", expenses.stream()
            .mapToDouble(expense -> expense.getAmount())
            .sum());
        result.put("expenseCount", expenses.size());
        
        return ResponseEntity.ok(result);
    }
}

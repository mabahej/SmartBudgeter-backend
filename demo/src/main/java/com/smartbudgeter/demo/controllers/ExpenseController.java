package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
}

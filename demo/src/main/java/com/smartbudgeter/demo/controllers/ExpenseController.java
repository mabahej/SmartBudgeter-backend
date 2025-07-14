package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController  {

    @Autowired
    private ExpenseRepository repository;

    @PostMapping
    public Expense save(@RequestBody Expense expense) {
        return repository.save(expense);
    }

    @GetMapping
    public List<Expense> all() {
        return repository.findAll();
    }
}

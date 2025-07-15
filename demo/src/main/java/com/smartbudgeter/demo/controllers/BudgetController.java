package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
      @Autowired
    private BudgetRepository repository;

    @PostMapping
    public Budget save(@RequestBody Budget budget) {
        return repository.save(budget);
    }

    @GetMapping
    public List<Budget> all() {
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
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getById(@PathVariable int id) {
        return repository.findById(id)
                .map(budget -> ResponseEntity.ok().body(budget))
                .orElse(ResponseEntity.notFound().build());

}
    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable int id, @RequestBody Budget budget) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        budget.setBudgetId(id);
        Budget updatedBudget = repository.save(budget);
        return ResponseEntity.ok(updatedBudget);
}
}

package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository repository;

    @PostMapping
    public Category save(@RequestBody Category category) {
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
}

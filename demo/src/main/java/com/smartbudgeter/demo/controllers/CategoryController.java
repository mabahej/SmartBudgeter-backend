package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private UserRepository repository;

    @PostMapping
    public User save(@RequestBody User expense) {
        return repository.save(expense);
    }

    @GetMapping
    public List<User> all() {
        return repository.findAll();
    }
}

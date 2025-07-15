package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    @Autowired
    private ReminderRepository repository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Reminder create(@RequestBody Reminder reminder) {
        User user = userRepository.findById(reminder.getUser().getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        reminder.setUser(user);
        return repository.save(reminder);
    }


    @GetMapping
    public List<Reminder> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reminder> getById(@PathVariable int id) {
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Reminder> update(@PathVariable int id, @RequestBody Reminder updated) {
        return repository.findById(id).map(reminder -> {
            reminder.setTitle(updated.getTitle());           // example field
            reminder.setDueDate(updated.getDueDate());       // example field
            reminder.setMessage(updated.getMessage());             // optional
            return ResponseEntity.ok(repository.save(reminder));
        }).orElse(ResponseEntity.notFound().build());
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

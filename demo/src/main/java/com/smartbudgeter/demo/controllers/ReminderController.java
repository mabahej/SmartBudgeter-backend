package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.Reminder;
import com.smartbudgeter.demo.models.User;
import com.smartbudgeter.demo.repositories.ReminderRepository;
import com.smartbudgeter.demo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String displayName = authentication.getName();
        return userRepository.findByDisplayName(displayName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/user")
    public List<Reminder> getCurrentUserReminders(Authentication authentication) {
        User user = getCurrentUser(authentication);
        System.out.println("User ID: " + user.getId());
System.out.println("Reminders: " + reminderRepository.findByUserId(user.getId()));

        return reminderRepository.findByUserId(user.getId());
    }

    @PostMapping
    public ResponseEntity<Reminder> createReminder(@RequestBody Reminder reminder, Authentication authentication) {
        User user = getCurrentUser(authentication);
        reminder.setUser(user);
        Reminder saved = reminderRepository.save(reminder);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<? extends Object> updateReminder(
            @PathVariable int id,
            @RequestBody Reminder updatedReminder,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return reminderRepository.findById(id).map(existing -> {
            if (!(existing.getUser().getId()==(currentUser.getId()))) {
                return ResponseEntity.status(403).build(); // Access denied
            }
            existing.setTitle(updatedReminder.getTitle());
            existing.setDueDate(updatedReminder.getDueDate());
            existing.setMessage(updatedReminder.getMessage());
            return ResponseEntity.ok(reminderRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteReminder(@PathVariable int id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return reminderRepository.findById(id).map(reminder -> {
            if (!(reminder.getUser().getId()==(user.getId()))) {
                return ResponseEntity.status(403).build();
            }
            reminderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}

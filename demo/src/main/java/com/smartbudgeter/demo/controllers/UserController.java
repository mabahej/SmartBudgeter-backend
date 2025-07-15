package com.smartbudgeter.demo.controllers;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/register")
public class UserController {

    @Autowired
    private UserRepository repository;

    @PostMapping
    public User save(@RequestBody User user) {
        return repository.save(user);
    }

    @GetMapping
    public List<User> all() {
        return repository.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) {
        return  repository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable int id, @RequestBody User updated) {
        return repository.findById(id).map(user -> {
            user.setDisplayName(updated.getDisplayName());           
            user.setEmail(updated.getEmail());       
            user.setPassword(updated.getPassword()); 
            user.setFamilyMembers(updated.getFamilyMembers());  
            user.setFamilyName(updated.getFamilyName());      
            user.setGoogleId(updated.getGoogleId()); 
            return ResponseEntity.ok(repository.save(user));
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

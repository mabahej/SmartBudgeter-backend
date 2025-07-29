package com.smartbudgeter.demo.controller;

import com.smartbudgeter.demo.config.CustomUserDetails;
import com.smartbudgeter.demo.dto.CategoryResponseDTO;
import com.smartbudgeter.demo.model.Category;
import com.smartbudgeter.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @PostMapping()
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            long userId = userDetails.getId();
            Category category = categoryService.createCategory(userId, request.getName());
            CategoryResponseDTO responseDTO = new CategoryResponseDTO(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.badRequest().body("Error creating category: " + e.getMessage());
        }
    }
    
    // Get all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
            .map(category -> ResponseEntity.ok().body(category))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Get categories by user
    @GetMapping("/user")
    public ResponseEntity<?> getCategoriesByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
   if (userDetails == null) {
            System.out.println("UserDetails is null! User not authenticated.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            long userId = userDetails.getId();
            System.out.println(">> Entered /user GET endpoint");
            List<Category> categories = categoryService.getCategoriesByUserId(userId);
            System.out.println("Fetching categories for user ID: " + userId);
            List<CategoryResponseDTO> responseDTOs = categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error retrieving categories: " + e.getMessage());
        }
    }
    
    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request.getName(), request.getDescription());
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating category: " + e.getMessage());
        }
    }
    
    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().body("Category deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting category: " + e.getMessage());
        }
    }
    
    // Search categories by name
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
    
    // Search categories by user and name
    @GetMapping("/search/user/{userId}")
    public ResponseEntity<List<Category>> searchCategoriesByUserId(@PathVariable Long userId, @RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByUserIdAndName(userId, name);
        return ResponseEntity.ok(categories);
    }
    
    // Get categories with active budgets
    @GetMapping("/with-budgets")
    public ResponseEntity<List<Category>> getCategoriesWithActiveBudgets() {
        List<Category> categories = categoryService.getCategoriesWithActiveBudgets();
        return ResponseEntity.ok(categories);
    }
    
    // Get categories without budgets for user
    @GetMapping("/without-budgets/user")
public ResponseEntity<?> getCategoriesWithoutBudgetsByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
    long userId = userDetails.getId();
        try {
            List<Category> categories = categoryService.getCategoriesWithoutBudgetsByUserId(userId);
            List<CategoryResponseDTO> responseDTOs = categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error retrieving categories: " + e.getMessage());
        }
    }

    // DTO Classes
    public static class CreateCategoryRequest {
        private String name;
        
        // Getters and setters
    
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
      
    }
    
    public static class UpdateCategoryRequest {
        private String name;
        private String description;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

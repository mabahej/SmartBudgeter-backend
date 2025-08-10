package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.*; 
import com.smartbudgeter.demo.repositories.*; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional; 

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryRepository categoryRepository; 

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository; 

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
             logger.warn("Authentication is null or not authenticated in getCurrentUser");
             throw new RuntimeException("User not authenticated"); 
        }

        String username = authentication.getName();
        logger.debug("Getting current user by username/displayName: {}", username);

        Optional<User> userOpt = userRepository.findByDisplayName(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
             logger.error("User not found in database for authenticated username/displayName: {}", username);
             throw new RuntimeException("Authenticated user not found in database");
        }
    }
    


    // 1. Create Category - Associate with the current user
    @PostMapping // Maps to POST /api/categories
    public ResponseEntity<Category> create(@RequestBody Category category, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to create category", currentUser.getId());

            category.setUser(currentUser);

           
            Category savedCategory = categoryRepository.save(category);
            logger.info("Category created successfully for user {}, category ID: {}", currentUser.getId(), savedCategory.getId());
            return ResponseEntity.ok(savedCategory);
        } catch (RuntimeException e) {
            logger.error("Error creating category for user", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. Get All Categories for the CURRENT User
    @GetMapping("/user") // Maps to GET /api/categories/user
    public ResponseEntity<List<Category>> getCurrentUserCategories(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("Fetching categories for user {}", currentUser.getId());

            List<Category> userCategories = categoryRepository.findByUserId(currentUser.getId());
            return ResponseEntity.ok(userCategories);
        } catch (RuntimeException e) {
            logger.error("Error fetching categories for user", e);
            return ResponseEntity.internalServerError().build(); // Return 500 error response
        }
    }

    // 3. Get a specific category by ID (Check ownership)
    @GetMapping("/{id}") // Maps to GET /api/categories/{id}
    public ResponseEntity<Category> getById(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} requesting category ID {}", currentUser.getId(), id);

            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                if (category.getUser() != null && category.getUser().getId() == currentUser.getId()) {
                    return ResponseEntity.ok(category);
                } else {
                    logger.warn("User {} attempted to access category {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); // Forbidden
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching category ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. Update a specific category by ID (Check ownership)
    @PutMapping("/{id}") // Maps to PUT /api/categories/{id}
    public ResponseEntity<Category> update(@PathVariable int id, @RequestBody Category categoryDetails, Authentication authentication) {
         try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to update category ID {}", currentUser.getId(), id);

            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isPresent()) {
                Category categoryToUpdate = categoryOpt.get();
                if (categoryToUpdate.getUser() != null && categoryToUpdate.getUser().getId() == currentUser.getId()) {
                   
                    categoryToUpdate.setName(categoryDetails.getName()); 
                   

                    Category updatedCategory = categoryRepository.save(categoryToUpdate);
                    logger.info("Category ID {} updated successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.ok(updatedCategory);
                } else {
                    logger.warn("User {} attempted to update category {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error updating category ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. Delete a specific category by ID (Check ownership)
    @DeleteMapping("/{id}") // Maps to DELETE /api/categories/{id}
    public ResponseEntity<Void> delete(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to delete category ID {}", currentUser.getId(), id);

            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isPresent()) {
                Category categoryToDelete = categoryOpt.get();
                if (categoryToDelete.getUser() != null && categoryToDelete.getUser().getId() == currentUser.getId()) {
                   
                    categoryRepository.deleteById(id);
                    logger.info("Category ID {} deleted successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.noContent().build();
                } else {
                    logger.warn("User {} attempted to delete category {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error deleting category ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 6. Get categories with expense summary for the CURRENT user
    @GetMapping("/summary/user") // Maps to GET /api/categories/summary/user
    public ResponseEntity<List<Map<String, Object>>> getCurrentUserCategoriesWithExpensesSummary(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            int userId = currentUser.getId(); 
            logger.info("Fetching category summary for user {}", userId);

            List<Category> categories = categoryRepository.findByUserId(userId); 

            List<Map<String, Object>> summary = categories.stream().map(category -> {
                List<Expense> expenses = expenseRepository.findByCategoryIdAndUserId(
                    category.getId(), userId); 

                Map<String, Object> categorySummary = new HashMap<>();
                categorySummary.put("categoryName", category.getName());
                categorySummary.put("categoryId", category.getId()); 
                categorySummary.put("totalAmount", expenses.stream()
                    .mapToDouble(Expense::getAmount) 
                    .sum());
                categorySummary.put("expenseCount", expenses.size());

                return categorySummary;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            logger.error("Error fetching category summary for user", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 7. Get specific category with its expenses (Check ownership)
    @GetMapping("/{id}/expenses") // Maps to GET /api/categories/{id}/expenses
    public ResponseEntity<Map<String, Object>> getCategoryWithExpenses(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} requesting category {} with expenses", currentUser.getId(), id);

            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                if (category.getUser() != null && category.getUser().getId() == currentUser.getId()) {

                    List<Expense> expenses = expenseRepository.findByCategoryIdAndUserId(id, currentUser.getId());

                    Map<String, Object> result = new HashMap<>();
                    result.put("category", category);
                    result.put("expenses", expenses);
                    result.put("totalAmount", expenses.stream()
                        .mapToDouble(Expense::getAmount) 
                        .sum());
                    result.put("expenseCount", expenses.size());

                    return ResponseEntity.ok(result);
                } else {
                     logger.warn("User {} attempted to access expenses for category {} belonging to another user.", currentUser.getId(), id);
                     return ResponseEntity.status(403).build(); // Forbidden
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching category ID {} with expenses for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

}

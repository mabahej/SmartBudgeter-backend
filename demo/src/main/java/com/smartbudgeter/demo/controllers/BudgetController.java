package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.*;
import com.smartbudgeter.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; 

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
   
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
             logger.warn("Authentication is null or not authenticated in getCurrentUser");
             throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName(); // Assuming displayName is used
        logger.debug("Getting current user by username/displayName: {}", username);

        Optional<User> userOpt = userRepository.findByDisplayName(username); // Adjust if needed
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
             logger.error("User not found in database for authenticated username/displayName: {}", username);
             throw new RuntimeException("Authenticated user not found in database");
        }
    }

    

@PostMapping // Maps to POST /api/budgets
public ResponseEntity<?> create(@RequestBody Budget budgetFromBody, Authentication authentication) {
    try {
        User currentUser = getCurrentUser(authentication);
        logger.info("User {} attempting to create budget", currentUser.getId());
        logger.debug("Raw Budget object received from frontend: {}", budgetFromBody);
        if (budgetFromBody != null) {
            try {
                logger.debug("Raw Budget object - categoryId (via getCategoryId): {}", budgetFromBody.getCategoryId());
            } catch (Exception e) {
                logger.debug("Raw Budget object - getCategoryId() threw exception: {}", e.getMessage());
            }
            try {
                logger.debug("Raw Budget object - category (via getCategory): {}", budgetFromBody.getCategoryId());
                if (budgetFromBody.getCategoryId() >0) {
                     logger.debug("Raw Budget object - category ID (via getCategory().getId()): {}", budgetFromBody.getCategoryId());
                }
            } catch (Exception e) {
                logger.debug("Raw Budget object - getCategory() or getCategory().getId() threw exception: {}", e.getMessage());
            }
            try {
                 logger.debug("Raw Budget object - Direct field categoryId: {}", budgetFromBody.getCategoryId()); // Only if field is accessible
            } catch (Exception e) {
                logger.debug("Raw Budget object - Direct field access threw exception: {}", e.getMessage());
            }
        }
    
        Integer categoryIdFromRequest = null;
        Float monthlyLimitFromRequest = null;

        try {
            categoryIdFromRequest = budgetFromBody.getCategoryId(); 
            monthlyLimitFromRequest = budgetFromBody.getMonthlyLimit(); 
            logger.debug("Extracted categoryId: {}, monthlyLimit: {} from request body for user {}",
                         categoryIdFromRequest, monthlyLimitFromRequest, currentUser.getId());
        } catch (Exception e) {
            logger.warn("Could not extract categoryId/monthlyLimit directly from Budget object structure for user {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body("Invalid request format. Missing categoryId or monthlyLimit.");
        }

        // --- 2. Validate extracted data ---
        if (categoryIdFromRequest == null) {
             logger.warn("CreateBudget request is missing categoryId for user {}", currentUser.getId());
             return ResponseEntity.badRequest().body("Category ID is required.");
        }
        if (monthlyLimitFromRequest == null || monthlyLimitFromRequest <= 0.0f) {
             logger.warn("CreateBudget request has invalid monthlyLimit for user {}", currentUser.getId());
             return ResponseEntity.badRequest().body("Monthly limit must be greater than zero.");
        }

        // --- 3. Find the Category by ID ---
        Optional<Category> categoryOpt = categoryRepository.findById(categoryIdFromRequest);
        if (categoryOpt.isEmpty()) {
             logger.warn("Category with ID {} not found for user {}", categoryIdFromRequest, currentUser.getId());
             return ResponseEntity.badRequest().body("Category with ID " + categoryIdFromRequest + " not found.");
        }
        Category category = categoryOpt.get();

        Budget newBudget = new Budget();
        newBudget.setMonthlyLimit(monthlyLimitFromRequest);
        // --- 4. Set the Category (if Budget has a 'Category' object) ---
        newBudget.setCreatedAt(LocalDateTime.now()); // Set the creation timestamp

        newBudget.setUser(currentUser);

        newBudget.setCategoryId(category.getId());
        System.out.println("++++++++++++++++++Category ID set to: " + newBudget.getCategoryId());
        System.out.println("++++++++++++++++++Actual  ID set to: " + categoryIdFromRequest);

        // Save the NEW Budget 
        Budget savedBudget = budgetRepository.save(newBudget);
        logger.info("Budget created successfully for user {}, budget ID: {}, linked to category ID: {}",
                    currentUser.getId(), savedBudget.getBudgetId(), savedBudget.getCategoryId()); // Assuming getCategoryId() accessor exists

        // Return the created budget entity 
        return ResponseEntity.ok(savedBudget);

    } catch (Exception e) { 
        logger.error("Unexpected error creating budget for user {}", getCurrentUser(authentication), e);
      
        return ResponseEntity.internalServerError().body("An error occurred while creating the budget.");
    }
}


    // 2. Get budgets for the CURRENT user
    @GetMapping("/user") // Maps to GET /api/budgets/user
    public ResponseEntity<List<Budget>> getCurrentUserBudgets(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("Fetching budgets for user {}", currentUser.getId());

            List<Budget> userBudgets = budgetRepository.findByUserId(currentUser.getId()); 
            return ResponseEntity.ok(userBudgets);
        } catch (RuntimeException e) {
            logger.error("Error fetching budgets for user", e);
            return ResponseEntity.internalServerError().build(); 
        }
    }

    // 3. Get a specific budget by ID (Check ownership)
    @GetMapping("/{id}") // Maps to GET /api/budgets/{id}
    public ResponseEntity<Budget> getById(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} requesting budget ID {}", currentUser.getId(), id);

            Optional<Budget> budgetOpt = budgetRepository.findById(id);
            if (budgetOpt.isPresent()) {
                Budget budget = budgetOpt.get();
                // Check ownership
                if (budget.getUser() != null && budget.getUser().getId() == currentUser.getId()) {
                    return ResponseEntity.ok(budget);
                } else {
                    logger.warn("User {} attempted to access budget {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching budget ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. Update a specific budget by ID (Check ownership)
    @PutMapping("/{id}") // Maps to PUT /api/budgets/{id}
    public ResponseEntity<Budget> update(@PathVariable int id, @RequestBody Budget budgetDetails, Authentication authentication) {
         try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to update budget ID {}", currentUser.getId(), id);

            Optional<Budget> budgetOpt = budgetRepository.findById(id);
            if (budgetOpt.isPresent()) {
                Budget budgetToUpdate = budgetOpt.get();
                if (budgetToUpdate.getUser() != null && budgetToUpdate.getUser().getId() == currentUser.getId()) {
                    budgetToUpdate.setMonthlyLimit(budgetDetails.getMonthlyLimit()); 

                    Budget updatedBudget = budgetRepository.save(budgetToUpdate);
                    logger.info("Budget ID {} updated successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.ok(updatedBudget);
                } else {
                    logger.warn("User {} attempted to update budget {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error updating budget ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. Delete a specific budget by ID (Check ownership)
    @DeleteMapping("/{id}") // Maps to DELETE /api/budgets/{id}
    public ResponseEntity<Void> delete(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to delete budget ID {}", currentUser.getId(), id);

            Optional<Budget> budgetOpt = budgetRepository.findById(id);
            if (budgetOpt.isPresent()) {
                Budget budgetToDelete = budgetOpt.get();
                // Check ownership
                if (budgetToDelete.getUser() != null && budgetToDelete.getUser().getId() == currentUser.getId()) {
                    budgetRepository.deleteById(id);
                    logger.info("Budget ID {} deleted successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.noContent().build();
                } else {
                    logger.warn("User {} attempted to delete budget {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); // Forbidden
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error deleting budget ID {} for user", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    

 
}

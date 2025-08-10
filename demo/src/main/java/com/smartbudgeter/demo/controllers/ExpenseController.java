package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.*;
import com.smartbudgeter.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository; 

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

    // --- 1. Create Expense - Associate with the current user ---
    @PostMapping // Maps to POST /api/expenses
    public ResponseEntity<Expense> create(@RequestBody Expense expenseFromBody, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to create expense", currentUser.getId());

            // --- 1. Validate Input ---
            if (expenseFromBody.getAmount() == null || expenseFromBody.getAmount() <= 0) {
                logger.warn("CreateExpense request has invalid amount for user {}", currentUser.getId());
                return ResponseEntity.badRequest().body(null); // Or return a specific error message
            }
            if (expenseFromBody.getDate() == null) {
                logger.warn("CreateExpense request is missing date for user {}", currentUser.getId());
                return ResponseEntity.badRequest().body(null);
            }
            if (expenseFromBody.getCategory() == null) {
                logger.warn("CreateExpense request is missing or has invalid category for user {}", currentUser.getId());
                return ResponseEntity.badRequest().body(null);
            }

            // --- 2. Find the Category by ID ---
            Integer categoryId = expenseFromBody.getCategory().getId();
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                logger.warn("Category with ID {} not found for user {}", categoryId, currentUser.getId());
                return ResponseEntity.badRequest().body(null); // Or specific error
            }
            Category category = categoryOpt.get();

          
            // --- 3. Create the Expense Entity ---
            Expense newExpense = new Expense();
            // Set fields from the request body
            newExpense.setAmount(expenseFromBody.getAmount());
            newExpense.setDate(expenseFromBody.getDate());

            // --- 5. Associate with User (Crucial for user_id FK) 
            newExpense.setUser(currentUser);

            // --- 6. Associate with Category (Crucial for category_id FK) 
            
            newExpense.setCategory(category);
            newExpense.setNote(expenseFromBody.getNote()); 

            // --- 7. Save the Expense 
            Expense savedExpense = expenseRepository.save(newExpense);
            logger.info("Expense created successfully for user {}",
                        currentUser.getId());

            return ResponseEntity.ok(savedExpense);

        } catch (Exception e) {
            logger.error("Unexpected error creating expense for user {}", getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().body(null); 
        }
    }

    // --- 2. Get All Expenses for the CURRENT User ---
    @GetMapping("/user") // Maps to GET /api/expenses/user
    public ResponseEntity<List<Expense>> getCurrentUserExpenses(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("Fetching expenses for user {}", currentUser.getId());

            List<Expense> userExpenses = expenseRepository.findByUserOrderByDateDesc(currentUser); 
            return ResponseEntity.ok(userExpenses);
        } catch (Exception e) {
            logger.error("Error fetching expenses for user {}", getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().build(); // Return 500 error response
        }
    }

    // --- 3. Get a specific expense by ID (Check ownership) ---
    @GetMapping("/{id}") // Maps to GET /api/expenses/{id}
    public ResponseEntity<Expense> getById(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} requesting expense ID {}", currentUser.getId(), id);

            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            if (expenseOpt.isPresent()) {
                Expense expense = expenseOpt.get();
                if (expense.getUser() != null && expense.getUser().getId() == currentUser.getId()) {
                    return ResponseEntity.ok(expense);
                } else {
                    logger.warn("User {} attempted to access expense {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); // Forbidden
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching expense ID {} for user {}", id, getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- 4. Update a specific expense by ID (Check ownership) ---
    @PutMapping("/{id}") // Maps to PUT /api/expenses/{id}
    public ResponseEntity<Expense> update(@PathVariable int id, @RequestBody Expense expenseDetails, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to update expense ID {}", currentUser.getId(), id);

            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            if (expenseOpt.isPresent()) {
                Expense expenseToUpdate = expenseOpt.get();
                if (expenseToUpdate.getUser() != null && expenseToUpdate.getUser().getId() == currentUser.getId()) {
                   
                    if (expenseDetails.getAmount() != null && expenseDetails.getAmount() > 0) {
                        expenseToUpdate.setAmount(expenseDetails.getAmount());
                    }
                
                    if (expenseDetails.getDate() != null) {
                        expenseToUpdate.setDate(expenseDetails.getDate());
                    }
                    if (expenseDetails.getCategory() != null) {
                        Integer newCategoryId = expenseDetails.getCategory().getId();
                        Optional<Category> newCategoryOpt = categoryRepository.findById(newCategoryId);
                        if (newCategoryOpt.isPresent()) {
                            Category newCategory = newCategoryOpt.get();
                            
                            expenseToUpdate.setCategory(newCategory); 
                        } else {
                            logger.warn("New category with ID {} not found for user {}", newCategoryId, currentUser.getId());
                            return ResponseEntity.badRequest().body(null); 
                        }
                    }

                    Expense updatedExpense = expenseRepository.save(expenseToUpdate);
                    logger.info("Expense ID {} updated successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.ok(updatedExpense);
                } else {
                    logger.warn("User {} attempted to update expense {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating expense ID {} for user {}", id, getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- 5. Delete a specific expense by ID (Check ownership) ---
    @DeleteMapping("/{id}") // Maps to DELETE /api/expenses/{id}
    public ResponseEntity<Void> delete(@PathVariable int id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            logger.info("User {} attempting to delete expense ID {}", currentUser.getId(), id);

            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            if (expenseOpt.isPresent()) {
                Expense expenseToDelete = expenseOpt.get();
                // Check ownership
                if (expenseToDelete.getUser() != null && expenseToDelete.getUser().getId() == currentUser.getId()) {
                    expenseRepository.deleteById(id);
                    logger.info("Expense ID {} deleted successfully for user {}", id, currentUser.getId());
                    return ResponseEntity.noContent().build();
                } else {
                    logger.warn("User {} attempted to delete expense {} belonging to another user.", currentUser.getId(), id);
                    return ResponseEntity.status(403).build(); 
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting expense ID {} for user {}", id, getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- 6. Get total expenses for the last month for the CURRENT user ---
    @GetMapping("/last-month-total/user") // Maps to GET /api/expenses/last-month-total/user
    public ResponseEntity<Double> getLastMonthTotalForCurrentUser(Authentication authentication) { 
        try {
            User currentUser = getCurrentUser(authentication);
            int userId = currentUser.getId();
            logger.info("Fetching last month's total expenses for user {}", userId);

            LocalDate today = LocalDate.now();
            LocalDate firstDayOfLastMonth = today.minusMonths(0).withDayOfMonth(1);
            LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

            Float total = expenseRepository.getTotalExpensesForLastMonth(userId, firstDayOfLastMonth, lastDayOfLastMonth);

            Double totalDouble = (total == null) ? 0.0 : total.doubleValue();
            logger.info("Total expenses for user {} for last month ({} to {}): {}", userId, firstDayOfLastMonth, lastDayOfLastMonth, totalDouble);
            return ResponseEntity.ok(totalDouble);
        } catch (Exception e) {
            logger.error("Error fetching last month's total expenses for user {}", getCurrentUserIdFromAuth(authentication), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    // --- Helper method to safely get user ID for logging in catch blocks ---
    private Integer getCurrentUserIdFromAuth(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                User user = getCurrentUser(authentication);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            logger.error("Error getting current user ID from authentication", e);
        }
        return null;
    }
}
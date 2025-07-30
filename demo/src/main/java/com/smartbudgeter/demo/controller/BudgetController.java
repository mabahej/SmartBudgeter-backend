// BudgetController.java
package com.smartbudgeter.demo.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.smartbudgeter.demo.config.CustomUserDetails;
import com.smartbudgeter.demo.dto.BudgetResponse;
import com.smartbudgeter.demo.dto.BudgetResponseDTO;
import com.smartbudgeter.demo.model.Budget;
import com.smartbudgeter.demo.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);   
    @Autowired
    private BudgetService budgetService;
    
    // Create budget
    @PostMapping
 public ResponseEntity<?> createBudget(@Valid @RequestBody CreateBudgetRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            long userId = userDetails.getId();
            Budget budget = budgetService.createBudget(userId, request.getCategoryId(), request.getMonthlyLimit());
            BudgetResponseDTO responseDTO = new BudgetResponseDTO(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.badRequest().body("Error creating budget: " + e.getMessage());
        }
    }
    
    // Get all budgets
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }
    
    @GetMapping("/{id}")
public ResponseEntity<?> getBudgetById(@PathVariable Long id) {
    try {
        Budget budget = budgetService.getBudgetById(id)
            .orElseThrow(() -> new NoSuchElementException("Budget not found with ID: " + id));

        BudgetResponseDTO responseDTO = new BudgetResponseDTO(budget);

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);

    } catch (NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Budget not found: " + e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
    }
}

    // Get budgets by user
@GetMapping("/user")
public ResponseEntity<?> getBudgetsForCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println(">> Entered /user GET endpoint");

        if (userDetails == null) {
            System.out.println(">> ERROR: userDetails is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        Long userId = userDetails.getId();
        System.out.println(">> userDetails.getId() = " + userId);

        if (userId == null) {
            System.out.println(">> ERROR: userId is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID is missing");
        }

        try {
            System.out.println(">> Type of userId = " + userDetails.getId().getClass().getName());
            System.out.println(">> Calling budgetService.getBudgetsByUserId(" + userId + ")");
            List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
            System.out.println(">> Returned budgets list with size: " + budgets.size());

            List<BudgetResponseDTO> responseDTOs = budgets.stream()
                .map(BudgetResponseDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            System.out.println(">> EXCEPTION occurred while fetching budgets:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving budgets: " + e.getMessage());
        }
    }


    @PostMapping("/alert-settings")
    public ResponseEntity<?> saveAlertSettings(@Valid @RequestBody AlertSettingsRequest request,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                logger.error("Unauthorized access: userDetails is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
            }
            Long userId = userDetails.getId();
            budgetService.saveAlertSettings(userId, request.getWarningThreshold());
            return ResponseEntity.ok().body("Alert settings saved successfully");
        } catch (Exception e) {
            logger.error("Error saving alert settings: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error saving alert settings: " + e.getMessage());
        }
    }
    
    // Get alert settings
    @GetMapping("/alert-settings")
    public ResponseEntity<?> getAlertSettings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                logger.error("Unauthorized access: userDetails is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
            }
            Long userId = userDetails.getId();
            BigDecimal warningThreshold = budgetService.getAlertSettings(userId);
            AlertSettingsRequest response = new AlertSettingsRequest();
            response.setWarningThreshold(warningThreshold);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            logger.warn("Alert settings not found for user ID: {}", userDetails.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert settings not found: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving alert settings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving alert settings: " + e.getMessage());
        }
    }

    // Existing DTO classes (CreateBudgetRequest, UpdateBudgetRequest, AddSpendingRequest) remain unchanged

    // Add AlertSettingsRequest DTO
    public static class AlertSettingsRequest {
        private BigDecimal warningThreshold;

        public BigDecimal getWarningThreshold() { return warningThreshold; }
        public void setWarningThreshold(BigDecimal warningThreshold) { this.warningThreshold = warningThreshold; }
    }
    // Get budgets by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Budget>> getBudgetsByCategoryId(@PathVariable Long categoryId) {
        List<Budget> budgets = budgetService.getBudgetsByCategoryId(categoryId);
        return ResponseEntity.ok(budgets);
    }
    
    // Update budget
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable Long id, @Valid @RequestBody UpdateBudgetRequest request) {
        try {
            Budget budget = budgetService.updateBudget(id, request.getMonthlyLimit(), request.getSpent());
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating budget: " + e.getMessage());
        }
    }
    
    // Add spending to budget
    @PostMapping("/{id}/spend")
    public ResponseEntity<?> addSpending(@PathVariable Long id, @Valid @RequestBody AddSpendingRequest request) {
        try {
            BudgetResponse budget = budgetService.addSpendingAndReturnDTO(id, request.getAmount());
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding spending: " + e.getMessage());
        }
    }
    
    // Delete budget
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        try {
            System.err.println(">> Deleting budget with ID:l " + id);
            System.err.println("-------------------------------------------------------------------------------------------------");
            budgetService.deleteBudget(id);
            return ResponseEntity.ok().body("Budget deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting budget: " + e.getMessage());
        }
    }
    
    // Get over-budget budgets
    @GetMapping("/over-budget")
    public ResponseEntity<List<Budget>> getOverBudgetBudgets() {
        List<Budget> budgets = budgetService.getOverBudgetBudgets();
        return ResponseEntity.ok(budgets);
    }
    
    // Get over-budget budgets by user
    @GetMapping("/over-budget/user")
    public ResponseEntity<List<Budget>> getOverBudgetBudgetsByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
            Long userId = userDetails.getId();

        List<Budget> budgets = budgetService.getOverBudgetBudgetsByUserId(userId);
        return ResponseEntity.ok(budgets);
    }
    
    // Get budget summary for user
    @GetMapping("/summary/user")
    public ResponseEntity<BudgetService.BudgetSummary> getBudgetSummaryByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
            Long userId = userDetails.getId();

        BudgetService.BudgetSummary summary = budgetService.getBudgetSummaryByUserId(userId);
        return ResponseEntity.ok(summary);
    }
    
    // DTO Classes
    public static class CreateBudgetRequest {
        private Long categoryId;
        private BigDecimal monthlyLimit;
        
        // Getters and setters
       
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    }
    
    public static class UpdateBudgetRequest {
        private BigDecimal monthlyLimit;
        private BigDecimal spent;
        
        // Getters and setters
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
        public BigDecimal getSpent() { return spent; }
        public void setSpent(BigDecimal spent) { this.spent = spent; }
    }
    
    public static class AddSpendingRequest {
        private BigDecimal amount;
        
        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
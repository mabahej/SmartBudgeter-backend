package com.smartbudgeter.demo.service;

import com.smartbudgeter.demo.dto.BudgetResponse;
import com.smartbudgeter.demo.model.Budget;
import com.smartbudgeter.demo.model.User;
import com.smartbudgeter.demo.model.Category;
import com.smartbudgeter.demo.repository.BudgetRepository;
import com.smartbudgeter.demo.repository.UserRepository;
import com.smartbudgeter.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Create budget
    public Budget createBudget(Long userId, Long categoryId, BigDecimal monthlyLimit) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if budget already exists for this user and category
        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategoryAndIsDeletedFalse(user, category);
        if (existingBudget.isPresent()) {
            System.out.print(existingBudget.toString());
            System.out.println("Found budget: " + existingBudget);
            System.out.println("-------------------------------------------------------------------------------------------------");
            throw new RuntimeException("Budget already exists for this category");
        }
        
        Budget budget = new Budget(monthlyLimit, user, category);
        return budgetRepository.save(budget);
    }
    
    // Get all budgets
    public List<Budget> getAllBudgets() {
        return budgetRepository.findByIsDeletedFalse();
    }
    
    // Get budget by ID
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findByIdAndIsDeletedFalse(id);
    }
    
    // Get budgets by user
public List<Budget> getBudgetsByUserId(Long userId) {

        return budgetRepository.findByUserIdAndIsDeletedFalse(userId);
    }
    
    // Get budgets by category
    public List<Budget> getBudgetsByCategoryId(Long categoryId) {
        return budgetRepository.findByCategoryIdAndIsDeletedFalse(categoryId);
    }
    
    // Update budget
    public Budget updateBudget(Long id, BigDecimal monthlyLimit, BigDecimal spent) {
        Budget budget = budgetRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        if (monthlyLimit != null) {
            budget.setMonthlyLimit(monthlyLimit);
        }
        if (spent != null) {
            budget.setSpent(spent);
        }
        
        return budgetRepository.save(budget);
    }
    
    // Add spending to budget
    public Budget addSpending(Long id, BigDecimal amount) {
        Budget budget = budgetRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        budget.setSpent(budget.getSpent().add(amount));
        return budgetRepository.save(budget);
    }

    public BudgetResponse addSpendingAndReturnDTO(Long id, BigDecimal amount) {
        Budget budget = addSpending(id, amount);
        return new BudgetResponse(
            budget.getId(),
            budget.getCategory().getName(),
            String.valueOf(budget.getCategory().getId()),
            budget.getMonthlyLimit(),
            budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO,
            budget.getUser().getId(),
            budget.getCreatedAt()
        );
    }
    
    // Soft delete budget
    public void deleteBudget(Long id) {
        System.out.println(">> Deleting budget with ID: " + id);
        System.out.println("-------------------------------------------------------------------------------------------------");
        Budget budget = budgetRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        budget.setIsDeleted(true);
        budgetRepository.save(budget);
    }
    
    // Get over-budget budgets
    public List<Budget> getOverBudgetBudgets() {
        return budgetRepository.findOverBudgetBudgets();
    }
    
    // Get over-budget budgets by user
    public List<Budget> getOverBudgetBudgetsByUserId(Long userId) {
        return budgetRepository.findOverBudgetBudgetsByUserId(userId);
    }
    
    // Get budget summary for user
    public BudgetSummary getBudgetSummaryByUserId(Long userId) {
        BigDecimal totalLimit = budgetRepository.getTotalMonthlyLimitByUserId(userId);
        BigDecimal totalSpent = budgetRepository.getTotalSpentByUserId(userId);
        List<Budget> budgets = budgetRepository.findByUserIdAndIsDeletedFalse(userId);
        
        return new BudgetSummary(totalLimit, totalSpent, budgets.size());
    }
    
    // Inner class for budget summary
    public static class BudgetSummary {
        private BigDecimal totalMonthlyLimit;
        private BigDecimal totalSpent;
        private BigDecimal totalRemaining;
        private int budgetCount;
        
        public BudgetSummary(BigDecimal totalMonthlyLimit, BigDecimal totalSpent, int budgetCount) {
            this.totalMonthlyLimit = totalMonthlyLimit;
            this.totalSpent = totalSpent;
            this.totalRemaining = totalMonthlyLimit.subtract(totalSpent);
            this.budgetCount = budgetCount;
        }
        
        // Getters
        public BigDecimal getTotalMonthlyLimit() { return totalMonthlyLimit; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public BigDecimal getTotalRemaining() { return totalRemaining; }
        public int getBudgetCount() { return budgetCount; }
    }
}
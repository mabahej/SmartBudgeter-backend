package com.smartbudgeter.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetResponse {
    
    private Long id;
    private String category;
    private String categoryId;
    private BigDecimal monthlyLimit;
    private BigDecimal spent;
    private Long userId;
    private LocalDateTime createdAt;
    private BigDecimal remainingBudget;
    private boolean isOverBudget;
    private BigDecimal utilizationPercentage;
    
    // Constructors
    public BudgetResponse() {}
    
    public BudgetResponse(Long id, String category, String categoryId, BigDecimal monthlyLimit, 
                         BigDecimal spent, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
        this.spent = spent;
        this.userId = userId;
        this.createdAt = createdAt;
        this.remainingBudget = monthlyLimit.subtract(spent);
        this.isOverBudget = spent.compareTo(monthlyLimit) > 0;
        this.utilizationPercentage = calculateUtilizationPercentage();
    }
    
    private BigDecimal calculateUtilizationPercentage() {
        if (monthlyLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spent.divide(monthlyLimit, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(BigDecimal.valueOf(100));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
    
    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
    
    public BigDecimal getSpent() {
        return spent;
    }
    
    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }
    
    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }
    
    public boolean isOverBudget() {
        return isOverBudget;
    }
    
    public void setOverBudget(boolean overBudget) {
        isOverBudget = overBudget;
    }
    
    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }
    
    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}

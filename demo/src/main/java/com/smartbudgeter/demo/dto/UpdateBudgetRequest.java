package com.smartbudgeter.demo.dto;
import jakarta.validation.constraints.PositiveOrZero; // also needed
import java.math.BigDecimal;
public class UpdateBudgetRequest {
    
    private String category;
    private String categoryId;
    
    @PositiveOrZero(message = "Monthly limit must be positive or zero")
    private BigDecimal monthlyLimit;
    
    @PositiveOrZero(message = "Spent amount must be positive or zero")
    private BigDecimal spent;
    
    // Constructors
    public UpdateBudgetRequest() {}
    
    // Getters and Setters
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
}

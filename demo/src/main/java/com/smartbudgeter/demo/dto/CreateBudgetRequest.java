package com.smartbudgeter.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

// Request DTO for creating budget
public class CreateBudgetRequest {
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotBlank(message = "Category ID is required")
    private String categoryId;
    
    @NotNull(message = "Monthly limit is required")
    @PositiveOrZero(message = "Monthly limit must be positive or zero")
    private BigDecimal monthlyLimit;
    
    // Constructors
    public CreateBudgetRequest() {}
    
    public CreateBudgetRequest(String category, String categoryId, BigDecimal monthlyLimit) {
        this.category = category;
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
    }
    
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
}

// Request DTO for updating budget

// Response DTO

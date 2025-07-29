package com.smartbudgeter.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartbudgeter.demo.model.Budget;

public class BudgetResponseDTO {
    private Long id;
    private BigDecimal monthlyLimit;
    private BigDecimal spent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private Long userId; // Include user ID instead of the full User object
    private Long categoryId; // Include category ID instead of the full Category object

    public BudgetResponseDTO(Budget budget) {
        this.id = budget.getId();
        this.monthlyLimit = budget.getMonthlyLimit();
        this.spent = budget.getSpent();
        this.createdAt = budget.getCreatedAt();
        this.updatedAt = budget.getUpdatedAt();
        this.isDeleted = budget.getIsDeleted();
        this.userId = budget.getUser() != null ? budget.getUser().getId() : null;
        this.categoryId = budget.getCategory() != null ? budget.getCategory().getId() : null;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
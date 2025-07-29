package com.smartbudgeter.demo.models;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int budgetId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; 
    @Column(name = "amount")
    private float amount;
    @Column(name = "monthly_limit")
    private float monthlyLimit;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Budget() {}

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(float monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

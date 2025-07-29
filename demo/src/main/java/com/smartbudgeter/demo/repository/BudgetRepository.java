package com.smartbudgeter.demo.repository;

import com.smartbudgeter.demo.model.Budget;
import com.smartbudgeter.demo.model.User;
import com.smartbudgeter.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    // Find active budgets (not soft deleted)
    List<Budget> findByIsDeletedFalse();
    
    // Find budget by ID and not deleted
    Optional<Budget> findByIdAndIsDeletedFalse(Long id);
    
    // Find budgets by user
    List<Budget> findByUserAndIsDeletedFalse(User user);
    List<Budget> findByUserIdAndIsDeletedFalse(Long userId);
    
    // Find budgets by category
    List<Budget> findByCategoryAndIsDeletedFalse(Category category);
    List<Budget> findByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    // Find budget by user and category
    Optional<Budget> findByUserAndCategoryAndIsDeletedFalse(User user, Category category);
    Optional<Budget> findByUserIdAndCategoryIdAndIsDeletedFalse(Long userId, Long categoryId);
    
    // Find over-budget budgets
    @Query("SELECT b FROM Budget b WHERE b.isDeleted = false AND b.spent > b.monthlyLimit")
    List<Budget> findOverBudgetBudgets();
    
    // Find budgets by user that are over budget
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isDeleted = false AND b.spent > b.monthlyLimit")
    List<Budget> findOverBudgetBudgetsByUserId(@Param("userId") Long userId);
    
    // Find budgets with remaining amount less than specified
    @Query("SELECT b FROM Budget b WHERE b.isDeleted = false AND (b.monthlyLimit - b.spent) < :amount")
    List<Budget> findBudgetsWithLowRemaining(@Param("amount") BigDecimal amount);
    
    // Get total monthly limit for user
    @Query("SELECT COALESCE(SUM(b.monthlyLimit), 0) FROM Budget b WHERE b.user.id = :userId AND b.isDeleted = false")
    BigDecimal getTotalMonthlyLimitByUserId(@Param("userId") Long userId);
    
    // Get total spent for user
    @Query("SELECT COALESCE(SUM(b.spent), 0) FROM Budget b WHERE b.user.id = :userId AND b.isDeleted = false")
    BigDecimal getTotalSpentByUserId(@Param("userId") Long userId);
}

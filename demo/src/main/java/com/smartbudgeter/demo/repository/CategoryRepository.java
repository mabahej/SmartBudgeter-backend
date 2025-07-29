package com.smartbudgeter.demo.repository;

import com.smartbudgeter.demo.model.Category;
import com.smartbudgeter.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find active categories (not soft deleted)
    List<Category> findByIsDeletedFalse();
    
    // Find category by ID and not deleted
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
    
    // Find categories by user
    List<Category> findByUserAndIsDeletedFalse(User user);
    List<Category> findByUserIdAndIsDeletedFalse(Long userId);
    
    // Find category by name and user
    Optional<Category> findByNameAndUserAndIsDeletedFalse(String name, User user);
    Optional<Category> findByNameAndUserIdAndIsDeletedFalse(String name, Long userId);
    
    // Search categories by name
    @Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.name LIKE %:name%")
    List<Category> findByNameContainingAndIsDeletedFalse(@Param("name") String name);
    
    // Search categories by user and name
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isDeleted = false AND c.name LIKE %:name%")
    List<Category> findByUserIdAndNameContainingAndIsDeletedFalse(@Param("userId") Long userId, @Param("name") String name);
    
    // Find categories with associated budgets
    @Query("SELECT DISTINCT c FROM Category c JOIN c.budgets b WHERE c.isDeleted = false AND b.isDeleted = false")
    List<Category> findCategoriesWithActiveBudgets();
    
    // Find categories without budgets for a user
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isDeleted = false AND c.id NOT IN (SELECT DISTINCT b.category.id FROM Budget b WHERE b.user.id = :userId AND b.isDeleted = false)")
    List<Category> findCategoriesWithoutBudgetsByUserId(@Param("userId") Long userId);
}

// ========== SERVICES ==========

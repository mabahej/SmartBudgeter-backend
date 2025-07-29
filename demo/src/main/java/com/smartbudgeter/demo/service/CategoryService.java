package com.smartbudgeter.demo.service;

import com.smartbudgeter.demo.model.Category;
import com.smartbudgeter.demo.model.User;
import com.smartbudgeter.demo.repository.CategoryRepository;
import com.smartbudgeter.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create category
    public Category createCategory(Long userId, String name) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if category with same name already exists for this user
        Optional<Category> existingCategory = categoryRepository.findByNameAndUserAndIsDeletedFalse(name, user);
        if (existingCategory.isPresent()) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        Category category = new Category(name, user);
        return categoryRepository.save(category);
    }
    
    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsDeletedFalse();
    }
    
    // Get category by ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findByIdAndIsDeletedFalse(id);
    }
    
    // Get categories by user
    public List<Category> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserIdAndIsDeletedFalse(userId);
    }
    
    // Update category
    public Category updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // Check if new name conflicts with existing category for same user
        if (name != null && !name.equals(category.getName())) {
            Optional<Category> existingCategory = categoryRepository.findByNameAndUserAndIsDeletedFalse(name, category.getUser());
            if (existingCategory.isPresent()) {
                throw new RuntimeException("Category with this name already exists");
            }
            category.setName(name);
        }
        
       
        
        return categoryRepository.save(category);
    }
    
    // Soft delete category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }
    
    // Search categories by name
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingAndIsDeletedFalse(name);
    }
    
    // Search categories by user and name
    public List<Category> searchCategoriesByUserIdAndName(Long userId, String name) {
        return categoryRepository.findByUserIdAndNameContainingAndIsDeletedFalse(userId, name);
    }
    
    // Get categories with active budgets
    public List<Category> getCategoriesWithActiveBudgets() {
        return categoryRepository.findCategoriesWithActiveBudgets();
    }
    
    // Get categories without budgets for user
    public List<Category> getCategoriesWithoutBudgetsByUserId(Long userId) {
        return categoryRepository.findCategoriesWithoutBudgetsByUserId(userId);
    }
}
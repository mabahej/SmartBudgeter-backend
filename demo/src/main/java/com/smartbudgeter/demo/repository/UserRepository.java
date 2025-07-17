package com.smartbudgeter.demo.repository;

import com.smartbudgeter.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Original methods (update these to check for soft delete)
    Optional<User> findByUsernameAndIsDeletedFalse(String username);
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    // For backward compatibility (update your existing code to use the above methods)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // New methods for user management
    List<User> findByIsDeletedFalse();
    List<User> findByIsDeletedTrue();
    Optional<User> findByIdAndIsDeletedFalse(Long id);
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND (u.username LIKE %:search% OR u.email LIKE %:search%)")
    List<User> findActiveUsersBySearch(@Param("search") String search);
    
    boolean existsByUsernameAndIsDeletedFalse(String username);
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    // Check if username/email exists for different user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId AND u.isDeleted = false")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId AND u.isDeleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}

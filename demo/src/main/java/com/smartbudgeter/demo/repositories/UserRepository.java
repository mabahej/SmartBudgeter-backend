package com.smartbudgeter.demo.repositories;

import java.util.List;
import java.util.Optional;
import com.smartbudgeter.demo.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByDisplayName(String displayName);
    
    Optional<User> findByDisplayNameAndIsDeletedFalse(String displayName);
    
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    // Existence check methods
    boolean existsByDisplayName(String displayName);
    boolean existsByEmail(String email);
    boolean existsByDisplayNameAndIsDeletedFalse(String displayName);
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    // New methods for user management
    List<User> findByIsDeletedFalse();
    List<User> findByIsDeletedTrue();
    Optional<User> findByIdAndIsDeletedFalse(Long id);
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND (u.displayName LIKE %:search% OR u.email LIKE %:search%)")
    List<User> findActiveUsersBySearch(@Param("search") String search);
    
    // Check if displayName/email exists for different user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.displayName = :displayName AND u.id != :userId AND u.isDeleted = false")
    boolean existsByDisplayNameAndIdNot(@Param("displayName") String displayName, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId AND u.isDeleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}
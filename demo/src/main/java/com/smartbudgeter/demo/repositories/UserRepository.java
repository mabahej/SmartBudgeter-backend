package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // You can add custom queries here if needed
}

package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByNameAndUser(String name,User user);
}

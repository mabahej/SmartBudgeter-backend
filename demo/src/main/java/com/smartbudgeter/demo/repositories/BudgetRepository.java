package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
List<Budget> findByUserId(int userId);
}

package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    // You can add custom queries here if needed
}

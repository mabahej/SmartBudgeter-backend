package com.smartbudgeter.demo.repositories;

import com.smartbudgeter.demo.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.smartbudgeter.demo.models.User;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
        List<Expense> findByUser(User user);

        @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
        float getTotalExpensesForLastMonth(@Param("userId") int userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

}

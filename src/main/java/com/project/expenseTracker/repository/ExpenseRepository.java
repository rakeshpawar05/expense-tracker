package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByMonthId(Long monthId);

    List<Expense> findByCategoryId(Long categoryId);

    void deleteByMonthId(Long monthId);
}

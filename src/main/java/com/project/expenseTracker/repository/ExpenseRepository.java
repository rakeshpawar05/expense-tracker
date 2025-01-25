package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByMonthId(Long monthId);

    List<Expense> findByCategoryId(Long categoryId);

    void deleteByMonthId(Long monthId);

    @Query("""
    select e from Expense e
    join Month m on m.id=e.month.id
    join Category c on c.id=e.category.id
    where (:monthName is null or m.name=:monthName)
    and (:categoryName is null or c.name=:categoryName)
    and (:expenseName is null or e.description=:expenseName)
    """)
    List<Expense> findByFilters(String monthName, String categoryName, String expenseName);

    List<Expense> findByMonthName(String monthName);
    List<Expense> findByCategoryName(String categoryName);
}

package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByMonthId(long monthId);

    void deleteByMonthId(Long monthId);

    List<Category> findByMonthName(String monthName);
}

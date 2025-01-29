package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByMonthId(long monthId);

    void deleteByMonthId(Long monthId);

    Optional<Category> findByNameAndMonthId(String name, long monthId);

    List<Category> findByMonthNameAndMonthYear(String monthName, int year);
}

package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByMonthId(long monthId);

    void deleteByMonthId(Long monthId);

    // Case-insensitive lookup so category names like "Food" and "food" match
    Optional<Category> findByNameIgnoreCaseAndMonthIdAndUserId(String name, long monthId, long userId);

    List<Category> findByMonthNameAndMonthYearAndUserId(String monthName, int year, long userId);

    List<Category> findByUserIdAndMonthYearNumAndMonthMonthNum(long userId, int yearNum, int monthNum);
}

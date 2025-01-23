package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

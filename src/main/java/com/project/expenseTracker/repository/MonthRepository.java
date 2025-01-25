package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthRepository extends JpaRepository<Month, Long> {
    List<Month> findByUserId(Long userId);
}

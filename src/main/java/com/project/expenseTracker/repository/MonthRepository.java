package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthRepository extends JpaRepository<Month, Long> {
}

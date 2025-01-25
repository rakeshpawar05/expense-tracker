package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthRepository extends JpaRepository<Month, Long> {
    List<Month> findByUserId(Long userId);

    Optional<Month> findByNameAndYear(String name, int year);

}

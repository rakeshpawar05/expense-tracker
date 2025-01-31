package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MonthRepository extends JpaRepository<Month, Long> {
    List<Month> findByUserId(Long userId);

    Optional<Month> findByNameAndYearAndUserId(String name, Integer year, Long userId);

    @Query("""
    select m from Month m
    join User u on u.id=m.user.id
    where (:name is null or m.name=:name)
    and (:year is null or m.year=:year)
    and (:userId is null or m.user.id=:userId)
    """)
    List<Month> findByFilters(Long userId, String name, Integer year);


}

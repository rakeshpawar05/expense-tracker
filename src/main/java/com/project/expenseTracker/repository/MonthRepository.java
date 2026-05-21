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

    Optional<Month> findByMonthNumAndYearNumAndUserId(Integer monthNum, Integer yearNum, Long userId);

    @Query("""
    select m from Month m
    join User u on u.id=m.user.id
    where (:monthNum is null or m.monthNum=:monthNum)
    and (:yearNum is null or m.yearNum=:yearNum)
    and (:userId is null or m.user.id=:userId)
    """)
    List<Month> findByFilters(Long userId, Integer monthNum, Integer yearNum);


}

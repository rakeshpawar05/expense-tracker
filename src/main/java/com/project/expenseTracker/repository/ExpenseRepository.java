package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByMonthId(Long monthId);
    List<Expense> findByEventId(Long eventId);

    List<Expense> findByCategoryId(Long categoryId);

    void deleteByMonthId(Long monthId);

    @Query("""
    select e from Expense e
    join Month m on m.id=e.month.id
    left join Category c on c.id=e.category.id
    where (:monthName is null or m.name=:monthName)
    and (:monthYear is null or m.year=:monthYear)
    and (:categoryName is null or c.name=:categoryName)
    and (:expenseName is null or e.description=:expenseName)
    and (:userId is null or e.user.id=:userId)
    """)
    List<Expense> findByFilters(Long userId, String monthName,Integer monthYear, String categoryName, String expenseName);

    List<Expense> findByMonthName(String monthName);
    List<Expense> findByCategoryName(String categoryName);

    @Query("""
    select e from Expense e
    join Month m on m.id=e.month.id
    left join Category c on c.id=e.category.id
    left join Event ev on ev.id=e.event.id
    where e.user.id=:userId
    and (:monthId is null or e.month.id=:monthId)
    and (:cursor is null or e.date < :cursor)
    and (:fromDate is null or e.date >= :fromDate)
    and (:toDate is null or e.date <= :toDate)
    and (:categoryId is null or e.category.id=:categoryId)
    and (:eventId is null or e.event.id=:eventId)
    order by e.date desc
    """)
    List<Expense> findFeedWithCursor(Long userId, Long monthId, String cursor, String fromDate, 
                                     String toDate, Long categoryId, Long eventId);

    @Query("""
    select e from Expense e
    join Month m on m.id=e.month.id
    left join Category c on c.id=e.category.id
    where e.user.id=:userId
    and (:monthId is null or e.month.id=:monthId)
    and (:categoryName is null or c.name=:categoryName)
    and (:expenseName is null or lower(cast(e.description as string)) like lower(concat('%', cast(:expenseName as string), '%')))
    and (:fromDate is null or e.date >= :fromDate)
    and (:toDate is null or e.date <= :toDate)
    order by e.date desc
    """)
    List<Expense> findSearchWithFilters(Long userId, Long monthId, String categoryName, String expenseName, 
                                        String fromDate, String toDate);

    @Query("""
    select count(e) from Expense e
    join Month m on m.id=e.month.id
    left join Category c on c.id=e.category.id
    where e.user.id=:userId
    and (:monthId is null or e.month.id=:monthId)
    and (:categoryName is null or c.name=:categoryName)
    and (:expenseName is null or lower(cast(e.description as string)) like lower(concat('%', cast(:expenseName as string), '%')))
    and (:fromDate is null or e.date >= :fromDate)
    and (:toDate is null or e.date <= :toDate)
    """)
    Long countSearchResults(Long userId, Long monthId, String categoryName, String expenseName, 
                            String fromDate, String toDate);
}

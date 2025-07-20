package com.project.expenseTracker.repository;

import com.project.expenseTracker.entity.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingRepository extends JpaRepository<Saving, Long> {

    List<Saving> findByMonthId(Long monthId);
    List<Saving> findByEventId(Long eventId);

    List<Saving> findByCategoryId(Long categoryId);

    void deleteByMonthId(Long monthId);

    @Query("""
    select e from Saving e
    join Month m on m.id=e.month.id
    left join Category c on c.id=e.category.id
    where (:monthName is null or m.name=:monthName)
    and (:monthYear is null or m.year=:monthYear)
    and (:categoryName is null or c.name=:categoryName)
    and (:savingName is null or e.description=:savingName)
    and (:userId is null or e.user.id=:userId)
    """)
    List<Saving> findByFilters(Long userId, String monthName,Integer monthYear, String categoryName, String savingName);

    List<Saving> findByMonthName(String monthName);
    List<Saving> findByCategoryName(String categoryName);
}

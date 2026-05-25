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
    where (:monthNum is null or m.monthNum=:monthNum)
    and (:yearNum is null or m.yearNum=:yearNum)
    and (:categoryName is null or lower(cast(c.name as string))=lower(cast(:categoryName as string)))
    and (:savingName is null or lower(cast(e.description as string))=lower(cast(:savingName as string)))
    and (:userId is null or e.user.id=:userId)
    """)
    List<Saving> findByFilters(Long userId, Integer monthNum,Integer yearNum, String categoryName, String savingName);

    List<Saving> findByMonthName(String monthName);
    List<Saving> findByCategoryNameIgnoreCase(String categoryName);
}

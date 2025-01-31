package com.project.expenseTracker.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDomainDto {
    private Long id;
    private String name;
    private List<MonthDto> months;
    private List<CategoryDto> categories;
    private List<ExpenseDto> expenses;
}

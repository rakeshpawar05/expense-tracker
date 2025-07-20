package com.project.expenseTracker.dto;

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
    private List<SavingDto> savings;
}

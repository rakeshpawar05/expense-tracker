package com.project.expenseTracker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DashboardDto {

    private String monthName;
    private Integer monthYear;
    private Double totalEarning;
    private Double totalExpense;
    private Double balance;
    private List<ExpenseDto> topExpenses;
    private List<CategorySummaryDto> categoryBreakdown;
    private Integer totalTransactions;

}


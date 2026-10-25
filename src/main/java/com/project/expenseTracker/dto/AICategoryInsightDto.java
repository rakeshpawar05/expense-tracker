package com.project.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AICategoryInsightDto {
    private String categoryName;
    private YearMonth yearMonth;
    private Double currentSpent;
    private Integer transactionCount;
    private Double averagePerTransaction;
    private Double percentOfTotalExpenses;
    private Double percentOfIncome;
    private Double previousMonthSpent;
    private Double percentageChangeVsPrevious;
    private String healthStatus;
    private Double recommendedBudgetCeiling;
    private String headline;
    private String insight;
    private String actionableTip;
    private String largestExpenseDescription;
    private Double largestExpenseAmount;
}

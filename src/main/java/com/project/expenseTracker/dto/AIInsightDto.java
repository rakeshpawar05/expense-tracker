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
public class AIInsightDto {
    private String title;
    private String periodType;
    private YearMonth analyzedYearMonth;
    private String headline;
    private String insight;
    private String keyTakeaway;
    private String statusTag;
    private Double totalEarning;
    private Double totalExpense;
    private Double balance;
    private Double spendPercentage;
    private String topCategory;
    private Double topCategoryAmount;
}

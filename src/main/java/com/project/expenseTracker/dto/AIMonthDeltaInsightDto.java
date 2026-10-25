package com.project.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMonthDeltaInsightDto {
    private String currentYearMonth;
    private String previousYearMonth;

    private Double currentTotalSpent;
    private Double previousTotalSpent;
    private Double absoluteChange;
    private Double percentageChange;

    private Double currentEarning;
    private Double previousEarning;
    private Double currentSavingsRate;
    private Double previousSavingsRate;

    private Integer currentTransactionCount;
    private Integer previousTransactionCount;

    private String topSurgeCategory;
    private Double topSurgeAmount;
    private Double topSurgePercentage;

    private String topCoolingCategory;
    private Double topCoolingAmount;
    private Double topCoolingPercentage;

    private List<CategoryMoMDeltaDto> categoryDeltas;

    private String verdictTag;
    private String headline;
    private String insight;
    private String takeaway;
}

package com.project.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIEventInsightDto {
    private Long eventId;
    private String eventName;
    private Double totalSpent;
    private Integer transactionCount;
    private Integer activeDays;
    private Double dailyBurnRate;
    private String startDate;
    private String endDate;
    private String largestExpenseDescription;
    private Double largestExpenseAmount;
    private String topCategory;
    private Double topCategoryAmount;
    private Map<String, Double> categoryBreakdown;
    private String verdictTag;
    private String headline;
    private String insight;
    private String takeaway;
}

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
public class AIParseExpenseRequest {
    private Long userId;
    private String text;
    private String defaultDate;
    private YearMonth yearMonth;
}

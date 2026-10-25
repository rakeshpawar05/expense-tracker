package com.project.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedExpenseDto {
    private String description;
    private Integer amount;
    private String date; // yyyy-MM-dd
    private String categoryName;
    private String eventName;
    private Double confidence;
    private String rawInput;
}

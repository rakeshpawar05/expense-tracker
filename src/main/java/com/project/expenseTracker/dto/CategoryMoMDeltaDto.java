package com.project.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMoMDeltaDto {
    private String categoryName;
    private Double currentAmount;
    private Double previousAmount;
    private Double absoluteChange;
    private Double percentageChange;
}

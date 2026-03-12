package com.project.expenseTracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CategorySummaryDto {

    private String categoryName;
    private Double totalAmount;
    private Integer expenseCount;

}


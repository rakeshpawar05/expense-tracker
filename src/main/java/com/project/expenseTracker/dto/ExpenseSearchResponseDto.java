package com.project.expenseTracker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExpenseSearchResponseDto {

    private List<ExpenseDto> items;
    
    private String nextCursor;
    
    private Boolean hasMore;
    
    private Long totalItems;

}


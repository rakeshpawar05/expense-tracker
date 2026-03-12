package com.project.expenseTracker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExpenseFeedResponseDto {

    private List<ExpenseFeedDto> items;
    
    private String nextCursor;
    
    private Boolean hasMore;

}


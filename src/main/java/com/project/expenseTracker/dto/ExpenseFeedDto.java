package com.project.expenseTracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExpenseFeedDto {

    private Long id;
    
    private String description;
    
    private Integer amount;
    
    private String date;
    
    private String monthName;
    
    private String categoryName;
    
    private String eventName;

}


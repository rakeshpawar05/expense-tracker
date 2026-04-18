package com.project.expenseTracker.dto;

import com.project.expenseTracker.enums.IntentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentResult {
    private IntentType intent;
    private String yearMonth;
    private String category;

}

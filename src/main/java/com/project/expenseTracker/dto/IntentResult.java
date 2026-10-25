package com.project.expenseTracker.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.expenseTracker.enums.IntentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentResult {
    private IntentType intent;

    @JsonAlias({"month", "year_month"})
    private String yearMonth;

    private String category;
    private String event;
}

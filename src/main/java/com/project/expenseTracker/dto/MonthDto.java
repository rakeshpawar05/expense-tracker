package com.project.expenseTracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthDto {
    @NotBlank(message = "Month name is required")
    private String name;

    @NotNull(message = "Earning is required")
    @Min(value = 0, message = "Earning must be greater than or equal to 0")
    private Double earning;

    private List<ExpenseDto> expenses;
}

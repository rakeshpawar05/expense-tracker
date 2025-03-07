package com.project.expenseTracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MonthDto {

    private Long id;

    @NotBlank(message = "Month name is required")
    private String name;

    @NotNull(message = "Earning is required")
    @Min(value = 0, message = "Earning must be greater than or equal to 0")
    private Double earning;

    @NotNull(message = "userId is required")
    private Long userId;

    private List<CategoryDto> categories;

    private List<ExpenseDto> expenses;

}

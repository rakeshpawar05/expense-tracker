package com.project.expenseTracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExpenseDto {

    private Long id;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private Integer amount;

    @NotNull(message = "Date is required")
    private String date;

    @NotNull(message = "Month Name is required")
    private String monthName;

    @NotNull(message = "Category Name is required")
    private String categoryName;
}

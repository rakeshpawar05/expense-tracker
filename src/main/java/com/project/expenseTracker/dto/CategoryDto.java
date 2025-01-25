package com.project.expenseTracker.dto;

import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "UserId is required")
    private Long userId;

    private Long monthId;

    private List<ExpenseDto> expenses;
}

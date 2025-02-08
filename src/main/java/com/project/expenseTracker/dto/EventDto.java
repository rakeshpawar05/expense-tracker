package com.project.expenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventDto {

    private Long id;

    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "UserId is required")
    private Long userId;

    private List<ExpenseDto> expenses;
}

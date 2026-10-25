package com.project.expenseTracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;

    @NotBlank(message = "Subscription name is required")
    private String name;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Integer amount;

    private String billingCycle; // MONTHLY, YEARLY, QUARTERLY, WEEKLY
    private Integer billingDay;  // 1 to 31
    private String categoryName;
    private String paymentMethod;
    private String status;        // ACTIVE, PAUSED, CANCELLED
    private String lastBilledDate;
    private String nextDueDate;
    private String notes;

    @NotNull(message = "userId is required")
    private Long userId;

    // Derived telemetry fields
    private Integer daysUntilDue;
    private Boolean isDueSoon;
    private Boolean isOverdue;
    private Boolean isLoggedThisMonth;
}

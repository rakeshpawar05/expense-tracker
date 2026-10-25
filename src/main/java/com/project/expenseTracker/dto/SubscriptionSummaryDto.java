package com.project.expenseTracker.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSummaryDto {
    private Double totalMonthlyCommitment;
    private Double totalAnnualCommitment;
    private Integer activeCount;
    private Integer pausedCount;
    private Integer dueSoonCount;
    private List<SubscriptionDto> subscriptions;
}

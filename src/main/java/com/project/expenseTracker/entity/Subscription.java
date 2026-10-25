package com.project.expenseTracker.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "billing_cycle", nullable = false)
    @Builder.Default
    private String billingCycle = "MONTHLY";

    @Column(name = "billing_day", nullable = false)
    @Builder.Default
    private Integer billingDay = 1;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "last_billed_date")
    private String lastBilledDate;

    @Column(name = "next_due_date")
    private String nextDueDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (billingCycle == null) {
            billingCycle = "MONTHLY";
        }
        if (billingDay == null) {
            billingDay = 1;
        }
    }
}

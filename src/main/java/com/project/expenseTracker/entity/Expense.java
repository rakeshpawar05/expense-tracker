package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer amount;

    @ManyToOne
    @JoinColumn(name = "month_id", nullable = false)
    private Month month;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

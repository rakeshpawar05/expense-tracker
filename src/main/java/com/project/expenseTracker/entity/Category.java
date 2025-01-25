package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(name = "UniqueCategoryForMonth", columnNames = { "name", "month_id" }) })
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "month_id", nullable = false)
    private Month month;

    @OneToMany(mappedBy = "category", orphanRemoval = true)
    private List<Expense> expenses;
}

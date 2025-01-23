package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "months")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Month {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double earning;

    @Column(nullable = false)
    private Long userId;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

//    @OneToMany(mappedBy = "month")
//    private List<Expense> expenses;
}

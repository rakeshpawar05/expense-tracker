package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.spi.CascadeStyle;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Month {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "month_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer year;

    @Column
    private Double earning;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "month")
    private List<Category> categories;

    @OneToMany(mappedBy = "month")
    private List<Expense> expenses;
}

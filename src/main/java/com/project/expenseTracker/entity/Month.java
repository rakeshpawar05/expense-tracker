package com.project.expenseTracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Month {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer year;

    @Column(name = "month_num")
    private Integer monthNum;
    @Column(name = "year_num")
    private Integer yearNum;

    @Column
    private Double earning;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonBackReference
    @OneToMany(mappedBy = "month", orphanRemoval = true)
    private List<Category> categories;

    @JsonBackReference
    @OneToMany(mappedBy = "month", orphanRemoval = true)
    private List<Expense> expenses;

    @JsonBackReference
    @OneToMany(mappedBy = "month", orphanRemoval = true)
    private List<Saving> savings;
}

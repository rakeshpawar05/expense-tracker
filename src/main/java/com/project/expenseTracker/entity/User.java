package com.project.expenseTracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

//    private Double earning;

    @JsonBackReference
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Month> months;

    @JsonBackReference
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Category> categories;

    @JsonBackReference
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Event> events;

    @JsonBackReference
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Expense> expenses;
}

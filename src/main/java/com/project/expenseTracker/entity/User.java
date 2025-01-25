package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
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

    private Double earning;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Month> months;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Category> categories;
}

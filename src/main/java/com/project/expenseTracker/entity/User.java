package com.project.expenseTracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Data
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

//    @OneToMany(mappedBy = "user")
//    @JoinColumn(referencedColumnName = "id")
//    private List<Month> months;

//    @OneToMany(mappedBy = "user")
//    private List<Category> categories;
}

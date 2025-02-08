package com.project.expenseTracker.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @Column(nullable = false)
    private String date;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "month_id", nullable = false)
    private Month month;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

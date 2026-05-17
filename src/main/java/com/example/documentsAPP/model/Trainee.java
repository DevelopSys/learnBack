package com.example.documentsAPP.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "trainees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "dni"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Data
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
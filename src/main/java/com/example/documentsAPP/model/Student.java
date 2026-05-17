package com.example.documentsAPP.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "dni")
        }
)
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;


    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
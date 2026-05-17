package com.example.documentsAPP.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "coordinators_school")
@Data
public class CoordinatorSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String surname;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    // Un coordinador puede tener varios cursos
    @OneToMany(mappedBy = "coordinator")
    @JsonIgnore
    private List<Course> courses;
}
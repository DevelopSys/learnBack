package com.example.documentsAPP.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "courses")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "LEVEL", nullable = false)
    private int level;

    @Column(name = "acronym", nullable = false, length = 20)
    private String acronym;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Student> students;

    // Coordinador del ciclo
    @ManyToOne
    @JoinColumn(name = "coordinator_id")
    private CoordinatorSchool coordinator;

    // NUEVO: resultados de aprendizaje asociados al ciclo
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LearningResult> learningResults;
}
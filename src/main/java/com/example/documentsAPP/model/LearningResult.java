package com.example.documentsAPP.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learning_results")
@Getter
@Setter
@NoArgsConstructor
public class LearningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false, length = 100)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false, length = 20)
    private String subjectName;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
}
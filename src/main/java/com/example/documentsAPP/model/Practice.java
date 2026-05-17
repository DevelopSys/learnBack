package com.example.documentsAPP.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "practices")
@Getter
@Setter
@NoArgsConstructor
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Empresa
    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    // Tutor de empresa (Trainee)
    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id")
    private Trainee trainee;

    @Column(name = "total_hours", nullable = false)
    private Integer totalHours;

    @Column(name = "daily_hours", nullable = false)
    private Integer dailyHours;

    // Hora de inicio y fin
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // Alumnos (1..N)
    @ManyToMany
    @JoinTable(
            name = "practice_students",
            joinColumns = @JoinColumn(name = "practice_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students = new HashSet<>();

    // Datos de convenio cacheados
    @Column(name = "agreement_number", nullable = false, length = 3)
    private String agreementNumber;

    @Column(name = "agreement_sign_date", nullable = false)
    private LocalDate agreementSignDate;

    // Datos de la práctica
    @Column(name = "workplace", nullable = false)
    private String workplace;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "schedule", nullable = false)
    private String schedule;
}
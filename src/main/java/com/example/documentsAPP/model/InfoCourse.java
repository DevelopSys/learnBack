package com.example.documentsAPP.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "info_course")
public class InfoCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "director_name", nullable = false)
    private String directorName;

    @Column(name = "director_last_name", nullable = false)
    private String directorLastName;

    @Column(name = "director_nif", nullable = false)
    private String directorNif;

    @Column(name = "school_number", nullable = false)
    private String schoolNumber;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "school_email", nullable = false)
    private String schoolEmail;

    @Column(name = "school_phone", nullable = false)
    private String schoolPhone;

    @Column(name = "school_address", nullable = false, length = 500)
    private String schoolAddress;

    @Column(name = "school_postal", nullable = false, length = 500)
    private String schoolPostalCode;

    @Column(name = "school_local", nullable = false, length = 500)
    private String schoolLocal;

    @Column(name = "school_state", nullable = false, length = 500)
    private String schoolState;

    @Column(name = "school_city", nullable = false, length = 500)
    private String schoolCity;

    @Column(name = "school_year", nullable = false)
    private String schoolYear;

    public InfoCourse(String directorName, String directorLastName, String schoolNumber, String schoolName, String schoolEmail, String schoolPhone, String schoolAddress, String schoolYear) {
        this.directorName = directorName;
        this.directorLastName = directorLastName;
        this.schoolNumber = schoolNumber;
        this.schoolName = schoolName;
        this.schoolEmail = schoolEmail;
        this.schoolPhone = schoolPhone;
        this.schoolAddress = schoolAddress;
        this.schoolYear = schoolYear;
    }
}
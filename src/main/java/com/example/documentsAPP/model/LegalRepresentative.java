package com.example.documentsAPP.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "legal_representatives")
@Data
public class LegalRepresentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "dni", nullable = false, length = 20)
    private String dni;


    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
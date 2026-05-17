package com.example.documentsAPP.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "agreement",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "number"),
                @UniqueConstraint(columnNames = "company_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true, length = 3)
    private String number;

    @Column(name = "sign_date", nullable = false)
    private LocalDate signDate;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"agreement", "representatives", "trainees"})
    private Company company;
}
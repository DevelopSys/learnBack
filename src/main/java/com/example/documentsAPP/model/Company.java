package com.example.documentsAPP.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nif;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(nullable = false)
    private String activity;

    @Column(nullable = false)
    private String street;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String city;

    // NUEVO
    @Column
    private String province;

    // NUEVO
    @Column
    private String country;

    @Column(nullable = false)
    private String phone;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Agreement agreement;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LegalRepresentative> representatives;

    public void mostrarDatos(){
        System.out.println("id = " + id);
        System.out.println("nif = " + nif);
        System.out.println("legalName = " + legalName);
        System.out.println("street = " + street);
        System.out.println("postalCode = " + postalCode);
        System.out.println("city = " + city);
        representatives.forEach(data-> System.out.println(data.getFullName()));
    }
}
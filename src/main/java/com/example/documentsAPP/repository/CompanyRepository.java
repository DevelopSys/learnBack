package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByNif(String nif);

    boolean existsByNifAndIdNot(String nif, Long id);

    Optional<Company> findByNif(String nif);


}
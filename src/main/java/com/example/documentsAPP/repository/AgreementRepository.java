package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {

    Optional<Agreement> findByCompanyId(Long companyId);

    Optional<Agreement> findTopByOrderByIdDesc();
    Optional<Agreement> findTopByOrderByNumberDesc();

    boolean existsByNumber(String number);
}
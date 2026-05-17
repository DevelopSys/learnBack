package com.example.documentsAPP.repository;


import com.example.documentsAPP.model.LegalRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalRepresentativeRepository extends JpaRepository<LegalRepresentative, Long> {
    List<LegalRepresentative> findByCompanyId(Long companyId);

}
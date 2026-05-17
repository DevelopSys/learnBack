package com.example.documentsAPP.service;

import com.example.documentsAPP.model.Agreement;
import com.example.documentsAPP.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgreementService {

    @Autowired
    private AgreementRepository agreementRepository;

    public List<Agreement> findAll() {
        return agreementRepository.findAll();
    }

    public Agreement findById(Long id) {
        return agreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agreement no encontrado con id: " + id));
    }

    public Agreement findByCompanyId(Long companyId) {
        return agreementRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("No existe agreement para la company con id: " + companyId));
    }

    public void delete(Long id) {
        Agreement agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agreement no encontrado con id: " + id));

        agreementRepository.delete(agreement);
    }
}
package com.example.documentsAPP.service;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.LegalRepresentative;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.repository.LegalRepresentativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LegalRepresentativeService {

    @Autowired
    private LegalRepresentativeRepository legalRepresentativeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public LegalRepresentative save(LegalRepresentative legalRepresentative) {
        return legalRepresentativeRepository.save(legalRepresentative);
    }

    public List<LegalRepresentative> findAll() {
        return legalRepresentativeRepository.findAll();
    }

    public Optional<LegalRepresentative> findById(Long id) {
        return legalRepresentativeRepository.findById(id);
    }

    public List<LegalRepresentative> findByCompanyId(Long companyId) {
        return legalRepresentativeRepository.findByCompanyId(companyId);
    }

    public Optional<LegalRepresentative> update(Long id, LegalRepresentative newData) {
        Optional<LegalRepresentative> optionalRepresentative = legalRepresentativeRepository.findById(id);

        if (optionalRepresentative.isEmpty()) {
            return Optional.empty();
        }

        LegalRepresentative representative = optionalRepresentative.get();
        representative.setFullName(newData.getFullName());
        representative.setDni(newData.getDni());

        if (newData.getCompany() != null && newData.getCompany().getId() != null) {
            Optional<Company> companyOptional = companyRepository.findById(newData.getCompany().getId());
            if (companyOptional.isPresent()) {
                representative.setCompany(companyOptional.get());
            }
        }

        return Optional.of(legalRepresentativeRepository.save(representative));
    }

    public void deleteById(Long id) {
        legalRepresentativeRepository.deleteById(id);
    }
}
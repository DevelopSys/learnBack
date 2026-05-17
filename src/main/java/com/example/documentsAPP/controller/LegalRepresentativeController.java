package com.example.documentsAPP.controller;


import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.LegalRepresentative;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.service.LegalRepresentativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/legal-representatives")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LegalRepresentativeController {

    @Autowired
    private LegalRepresentativeService legalRepresentativeService;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public ResponseEntity<?> createLegalRepresentative(@RequestBody LegalRepresentative legalRepresentative,
                                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        if (legalRepresentative.getCompany() == null || legalRepresentative.getCompany().getId() == null) {
            return ResponseEntity.badRequest().body("Debes indicar la empresa");
        }

        Optional<Company> companyOptional = companyRepository.findById(legalRepresentative.getCompany().getId());

        if (companyOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        legalRepresentative.setCompany(companyOptional.get());

        return ResponseEntity.ok(legalRepresentativeService.save(legalRepresentative));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLegalRepresentativeById(@PathVariable Long id,
                                                        Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<LegalRepresentative> representative = legalRepresentativeService.findById(id);

        if (representative.isEmpty()) {
            return ResponseEntity.status(404).body("Representante no encontrado");
        }

        return ResponseEntity.ok(representative.get());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getLegalRepresentativesByCompanyId(@PathVariable Long companyId,
                                                                Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Company> companyOptional = companyRepository.findById(companyId);

        if (companyOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        List<LegalRepresentative> representatives =
                legalRepresentativeService.findByCompanyId(companyId);

        return ResponseEntity.ok(representatives);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLegalRepresentative(@PathVariable Long id,
                                                       @RequestBody LegalRepresentative legalRepresentative,
                                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<LegalRepresentative> updatedRepresentative =
                legalRepresentativeService.update(id, legalRepresentative);

        if (updatedRepresentative.isEmpty()) {
            return ResponseEntity.status(404).body("Representante no encontrado");
        }

        return ResponseEntity.ok(updatedRepresentative.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLegalRepresentative(@PathVariable Long id,
                                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<LegalRepresentative> representative = legalRepresentativeService.findById(id);

        if (representative.isEmpty()) {
            return ResponseEntity.status(404).body("Representante no encontrado");
        }

        legalRepresentativeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

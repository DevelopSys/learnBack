package com.example.documentsAPP.controller;

import com.example.documentsAPP.dto.ConvenioAnexoData;
import com.example.documentsAPP.model.Agreement;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.repository.AgreementRepository;
import com.example.documentsAPP.service.AgreementService;
import com.example.documentsAPP.service.AnexoConvenioDocxService;
import com.example.documentsAPP.service.CompanyService;
import com.example.documentsAPP.service.ConvenioAnexoDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/agreements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgreementController {

    @Autowired
    private AgreementService agreementService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ConvenioAnexoDataBuilder anexoDataBuilder;

    @Autowired
    private AnexoConvenioDocxService anexoDocxService;



    @GetMapping
    public ResponseEntity<?> getAllAgreements(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(agreementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAgreementById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            Agreement agreement = agreementService.findById(id);
            return ResponseEntity.ok(agreement);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getAgreementByCompanyId(
            @PathVariable Long companyId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Company> company = companyService.findById(companyId);

        if (company.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        try {
            Agreement agreement = agreementService.findByCompanyId(companyId);
            return ResponseEntity.ok(agreement);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAgreement(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            agreementService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    @GetMapping("/{id}/anexo")
    public ResponseEntity<?> descargarAnexo(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            Agreement agreement = agreementService.findById(id);
            Company company = agreement.getCompany();

            ConvenioAnexoData data = anexoDataBuilder.build(company, agreement);

            byte[] docx = anexoDocxService.generarAnexo(data);

            String fileName = "Anexo1_" + agreement.getNumber() + ".docx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            return new ResponseEntity<>(docx, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generando el anexo");
        }
    }
}
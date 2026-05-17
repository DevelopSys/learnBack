package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.LegalRepresentative;
import com.example.documentsAPP.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<?> createCompany(@RequestBody Company company, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            return ResponseEntity.ok(companyService.save(company));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(companyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Company> company = companyService.findById(id);

        if (company.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        return ResponseEntity.ok(company.get());
    }

    @GetMapping("/{id}/representatives")
    public ResponseEntity<?> getRepresentativesByCompanyId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Company> company = companyService.findById(id);

        if (company.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        List<LegalRepresentative> representatives = companyService.getRepresentativesByCompanyId(id);
        return ResponseEntity.ok(representatives);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @RequestBody Company company,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            Company updatedCompany = companyService.update(id, company);

            if (updatedCompany == null) {
                return ResponseEntity.status(404).body("Empresa no encontrada");
            }

            return ResponseEntity.ok(updatedCompany);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        boolean deleted = companyService.deleteById(id);

        if (!deleted) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        return ResponseEntity.noContent().build();
    }

    // ---------- NUEVO: subir empresas desde CSV ----------
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCompaniesCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El fichero CSV está vacío");
        }

        try {
            int creadas = companyService.importFromCsv(file);
            return ResponseEntity.ok("Empresas importadas correctamente: " + creadas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al procesar CSV: " + e.getMessage());
        }
    }

    // ---------- NUEVO: plantilla CSV de empresas ----------
    @GetMapping(value = "/template", produces = "text/csv")
    public ResponseEntity<byte[]> downloadCompaniesTemplate(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String csvContent = companyService.generateCompaniesCsvTemplate();
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_empresas.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
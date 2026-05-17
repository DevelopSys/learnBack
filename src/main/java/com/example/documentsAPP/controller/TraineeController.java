package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.Trainee;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.service.TraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trainees")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TraineeController {

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public ResponseEntity<?> createTrainee(@RequestBody Trainee trainee, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            if (trainee.getCompany() == null || trainee.getCompany().getId() == null) {
                return ResponseEntity.badRequest().body("Debes indicar la empresa");
            }

            Optional<Company> companyOptional = companyRepository.findById(trainee.getCompany().getId());

            if (companyOptional.isEmpty()) {
                return ResponseEntity.status(404).body("Empresa no encontrada");
            }

            trainee.setCompany(companyOptional.get());

            return ResponseEntity.ok(traineeService.save(trainee));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTrainees(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(traineeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTraineeById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Trainee> trainee = traineeService.findById(id);

        if (trainee.isEmpty()) {
            return ResponseEntity.status(404).body("Tutor no encontrado");
        }

        return ResponseEntity.ok(trainee.get());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getTraineesByCompanyId(
            @PathVariable Long companyId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Optional<Company> companyOptional = companyRepository.findById(companyId);

        if (companyOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Empresa no encontrada");
        }

        List<Trainee> trainees = traineeService.findByCompanyId(companyId);
        return ResponseEntity.ok(trainees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrainee(
            @PathVariable Long id,
            @RequestBody Trainee trainee,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            Optional<Trainee> updatedTrainee = traineeService.update(id, trainee);

            if (updatedTrainee.isEmpty()) {
                return ResponseEntity.status(404).body("Tutor no encontrado");
            }

            return ResponseEntity.ok(updatedTrainee.get());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrainee(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        boolean deleted = traineeService.deleteById(id);

        if (!deleted) {
            return ResponseEntity.status(404).body("Tutor no encontrado");
        }

        return ResponseEntity.noContent().build();
    }
}
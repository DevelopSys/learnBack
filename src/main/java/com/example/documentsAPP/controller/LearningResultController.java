package com.example.documentsAPP.controller;

import com.example.documentsAPP.dto.LearningResultCreateRequest;
import com.example.documentsAPP.dto.LearningResultUpdateRequest;
import com.example.documentsAPP.model.LearningResult;
import com.example.documentsAPP.service.LearningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/learning-results")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LearningResultController {

    @Autowired
    private LearningResultService learningResultService;

    @GetMapping
    public ResponseEntity<?> getAllLearningResults(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String subjectCode,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        if (courseId != null) {
            return ResponseEntity.ok(learningResultService.findByCourseId(courseId));
        }

        if (subjectCode != null && !subjectCode.trim().isEmpty()) {
            return ResponseEntity.ok(learningResultService.findBySubjectCode(subjectCode));
        }

        return ResponseEntity.ok(learningResultService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLearningResultById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            LearningResult learningResult = learningResultService.findById(id);
            return ResponseEntity.ok(learningResult);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createLearningResult(
            @RequestBody LearningResultCreateRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            LearningResult created = learningResultService.create(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLearningResult(
            @PathVariable Long id,
            @RequestBody LearningResultUpdateRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            LearningResult updated = learningResultService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLearningResult(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            learningResultService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            int imported = learningResultService.importFromCsv(file);
            return ResponseEntity.ok("Resultados de aprendizaje importados correctamente: " + imported);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/template")
    public ResponseEntity<?> downloadCsvTemplate(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            String csv = learningResultService.generateCsvTemplate();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"plantilla_resultados_aprendizaje.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
package com.example.documentsAPP.controller;

import com.example.documentsAPP.dto.*;
import com.example.documentsAPP.model.Practice;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.service.*;
import com.example.documentsAPP.service.Anexo6DataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/practices")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private ConvenioAnexo4DataBuilder convenioAnexo4DataBuilder;

    @Autowired
    private Anexo4ConvenioDocxService anexo4ConvenioDocxService;

    @Autowired
    private Anexo6DataBuilder anexo6DataBuilder;

    @Autowired
    private Anexo6DocxService anexo6DocxService;

    @Autowired
    private Anexo8DataBuilder anexo8DataBuilder;

    @Autowired
    private Anexo8DocxService anexo8DocxService;

    @Autowired
    private Anexo9DataBuilder anexo9DataBuilder;

    @Autowired
    private Anexo9DocxService anexo9DocxService;

    @Autowired
    private GmailService gmailService;

    @Autowired
    private Anexo4ConvenioDocxService anexo4Service;

    @Autowired
    private Anexo6DocxService anexo6Service;

    @Autowired
    private Anexo8DocxService anexo8Service;

    @Autowired
    private Anexo9DocxService anexo9Service;

    @Autowired
    private ConvenioAnexo4DataBuilder anexo4DataBuilder;

    @Autowired
    private ConvenioAnexoDataBuilder anexoDataBuilder;





    // ─────────────────────────────────────────────────────────────────────
    // ANEXO 4 — Relación de alumnos
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/anexo4")
    public ResponseEntity<?> descargarAnexo4(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Usuario no autenticado");
        }

        try {
            Practice practice = practiceService.findById(id);
            if (practice == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Práctica no encontrada");
            }

            ConvenioAnexo4Data data = convenioAnexo4DataBuilder.build(practice);
            byte[] docx = anexo4ConvenioDocxService.generarAnexo4(data);

            String nombre = "Anexo4_" + (practice.getAgreementNumber() != null
                    ? practice.getAgreementNumber()
                    : practice.getId()) + ".docx";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .contentLength(docx.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombre + "\"")
                    .body(new ByteArrayResource(docx));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generando el anexo 4: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ANEXO 6
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/anexo6")
    public ResponseEntity<?> descargarAnexo6(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Usuario no autenticado");
        }

        try {
            Practice practice = practiceService.findById(id);
            if (practice == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Práctica no encontrada");
            }

            Student student = resolverAlumno(practice, studentId);
            Anexo6Data data = anexo6DataBuilder.build(practice, student);
            byte[] docx = anexo6DocxService.generarAnexo6(data);

            String nombre = "Anexo6_" +
                    limpiarNombre(student.getLastName() + "_" + student.getFirstName()) +
                    ".docx";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .contentLength(docx.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombre + "\"")
                    .body(new ByteArrayResource(docx));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generando el anexo 6: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ANEXO 8 — Ficha de seguimiento periódico
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/anexo8")
    public ResponseEntity<?> descargarAnexo8(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Usuario no autenticado");
        }

        try {
            Practice practice = practiceService.findById(id);
            if (practice == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Práctica no encontrada");
            }

            Student student = resolverAlumno(practice, studentId);

            Anexo8Data data = anexo8DataBuilder.build(practice, student); // ✅ tipo correcto
            byte[] docx = anexo8DocxService.generarAnexo8(data);

            String nombre = "Anexo8_" +
                    limpiarNombre(student.getLastName() + "_" + student.getFirstName()) +
                    ".docx";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .contentLength(docx.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombre + "\"")
                    .body(new ByteArrayResource(docx));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generando el anexo 8: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ANEXO 9 — Informe de valoración final del tutor de empresa
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/anexo9")
    public ResponseEntity<?> descargarAnexo9(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Usuario no autenticado");
        }

        try {
            Practice practice = practiceService.findById(id);
            if (practice == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Práctica no encontrada");
            }

            Student student = resolverAlumno(practice, studentId);
            Anexo9Data data = anexo9DataBuilder.build(practice, student);
            byte[] docx = anexo9DocxService.generarAnexo9(data);

            String nombre = "Anexo9_" +
                    limpiarNombre(student.getLastName() + "_" + student.getFirstName()) +
                    ".docx";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .contentLength(docx.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombre + "\"")
                    .body(new ByteArrayResource(docx));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generando el anexo 9: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllPractices(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
        return ResponseEntity.ok(practiceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPracticeById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Practice practice = practiceService.findById(id);
        if (practice == null) {
            return ResponseEntity.status(404).body("Práctica no encontrada");
        }
        return ResponseEntity.ok(practice);
    }

    @PostMapping
    public ResponseEntity<?> createPractice(
            @RequestBody PracticeCreateRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            Practice practice = practiceService.createPractice(request);
            return ResponseEntity.ok(practice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Practice> updatePractice(
            @PathVariable Long id,
            @RequestBody PracticeUpdateRequest request
    ) {
        Practice updated = practiceService.updatePractice(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePractice(@PathVariable Long id) {
        boolean deleted = practiceService.deletePractice(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────
    private Student resolverAlumno(Practice practice, Long studentId) {
        if (practice.getStudents() == null || practice.getStudents().isEmpty()) {
            throw new IllegalArgumentException("La práctica no tiene alumnos asignados");
        }

        List<Student> students = practice.getStudents().stream()
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName))
                .toList();

        if (studentId != null) {
            return students.stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El alumno indicado no pertenece a esta práctica"));
        }

        if (students.size() == 1) {
            return students.get(0);
        }

        throw new IllegalArgumentException(
                "La práctica tiene varios alumnos; debe indicar studentId");
    }

    private String limpiarNombre(String texto) {
        return texto == null ? "documento" : texto.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
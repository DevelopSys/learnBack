package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(studentService.save(student));
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Student student = studentService.findById(id);

        if (student == null) {
            return ResponseEntity.status(404).body("Alumno no encontrado");
        }

        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        boolean deleted = studentService.deleteById(id);

        if (!deleted) {
            return ResponseEntity.status(404).body("Alumno no encontrado");
        }

        return ResponseEntity.ok("Alumno eliminado correctamente");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Student updated = studentService.update(id, student);

        if (updated == null) {
            return ResponseEntity.status(404).body("Alumno no encontrado");
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllStudents(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        studentService.deleteAll();
        return ResponseEntity.ok("Todos los alumnos han sido eliminados");
    }

    // ---------- NUEVO: subir CSV de alumnos ----------
    @PostMapping("/upload")
    public ResponseEntity<?> uploadStudentsCsv(
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
            int creados = studentService.importFromCsv(file);
            return ResponseEntity.ok("Alumnos importados correctamente: " + creados);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al procesar CSV: " + e.getMessage());
        }
    }

    // ---------- NUEVO: plantilla CSV ----------
    @GetMapping(value = "/template", produces = "text/csv")
    public ResponseEntity<byte[]> downloadStudentsTemplate(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String csvContent = studentService.generateCsvTemplate();
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_alumnos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
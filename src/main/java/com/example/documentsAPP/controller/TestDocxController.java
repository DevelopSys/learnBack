package com.example.documentsAPP.controller;

import com.example.documentsAPP.service.Anexo6TemplateTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestDocxController {

    @Autowired
    private Anexo6TemplateTestService anexo6TemplateTestService;

    @GetMapping("/anexo6-template")
    public ResponseEntity<?> descargarPlantillaAnexo6(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            byte[] docx = anexo6TemplateTestService.descargarPlantillaReescrita();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"test_template_anexo6.docx\""
            );

            return new ResponseEntity<>(docx, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error generando el test de plantilla del anexo 6");
        }
    }
}
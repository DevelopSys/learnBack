package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    @Autowired
    private  CourseService courseService;



    @GetMapping
    public ResponseEntity<?> getAllCourses(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        return ResponseEntity.ok(courseService.findAll());
    }
}
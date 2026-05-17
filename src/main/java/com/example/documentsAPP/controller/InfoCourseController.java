package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.InfoCourse;
import com.example.documentsAPP.service.InfoCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/info-courses")
@CrossOrigin(origins = "*")
public class InfoCourseController {

    @Autowired
    private  InfoCourseService infoCourseService;


    @GetMapping
    public List<InfoCourse> findAll() {
        return infoCourseService.findAll();
    }

    @GetMapping("/{id}")
    public InfoCourse findById(@PathVariable Long id) {
        return infoCourseService.findById(id);
    }

    @PostMapping
    public ResponseEntity<InfoCourse> save(@RequestBody InfoCourse infoCourse) {
        InfoCourse saved = infoCourseService.save(infoCourse);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InfoCourse> update(@PathVariable Long id, @RequestBody InfoCourse infoCourse) {
        InfoCourse updated = infoCourseService.update(id, infoCourse);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        infoCourseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
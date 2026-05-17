package com.example.documentsAPP.service;

import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }



    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
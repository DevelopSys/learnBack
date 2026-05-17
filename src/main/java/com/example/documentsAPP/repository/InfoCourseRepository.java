package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.InfoCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoCourseRepository extends JpaRepository<InfoCourse, Long> {
}
package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.LearningResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningResultRepository extends JpaRepository<LearningResult, Long> {

    List<LearningResult> findByCourseIdOrderByNumberAsc(Long courseId);

    List<LearningResult> findBySubjectCodeOrderByNumberAsc(String subjectCode);

    boolean existsByCourseIdAndNumber(Long courseId, Integer number);
}
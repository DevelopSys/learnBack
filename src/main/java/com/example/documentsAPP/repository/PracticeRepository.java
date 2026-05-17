package com.example.documentsAPP.repository;


import com.example.documentsAPP.model.Practice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeRepository extends JpaRepository<Practice, Long> {
}
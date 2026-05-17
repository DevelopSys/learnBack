package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);
}
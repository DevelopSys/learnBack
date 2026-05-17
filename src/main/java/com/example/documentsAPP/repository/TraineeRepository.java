package com.example.documentsAPP.repository;

import com.example.documentsAPP.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    List<Trainee> findByCompanyId(Long companyId);

    Optional<Trainee> findByDni(String dni);
    Optional<Trainee> findByEmail(String email);

    boolean existsByDni(String dni);
    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}
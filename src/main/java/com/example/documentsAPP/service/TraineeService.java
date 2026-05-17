package com.example.documentsAPP.service;

import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.Trainee;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.repository.TraineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public Trainee save(Trainee trainee) {
        if (traineeRepository.existsByDni(trainee.getDni())) {
            throw new RuntimeException("Ya existe un tutor con ese DNI");
        }

        if (traineeRepository.existsByEmail(trainee.getEmail())) {
            throw new RuntimeException("Ya existe un tutor con ese email");
        }

        if (trainee.getCompany() == null || trainee.getCompany().getId() == null) {
            throw new RuntimeException("Debes indicar la empresa");
        }

        Optional<Company> companyOptional = companyRepository.findById(trainee.getCompany().getId());

        if (companyOptional.isEmpty()) {
            throw new RuntimeException("Empresa no encontrada");
        }

        trainee.setCompany(companyOptional.get());

        return traineeRepository.save(trainee);
    }

    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    public Optional<Trainee> findById(Long id) {
        return traineeRepository.findById(id);
    }

    public List<Trainee> findByCompanyId(Long companyId) {
        return traineeRepository.findByCompanyId(companyId);
    }

    public Optional<Trainee> update(Long id, Trainee newData) {
        Optional<Trainee> optionalTrainee = traineeRepository.findById(id);

        if (optionalTrainee.isEmpty()) {
            return Optional.empty();
        }

        if (traineeRepository.existsByDniAndIdNot(newData.getDni(), id)) {
            throw new RuntimeException("Ya existe otro tutor con ese DNI");
        }

        if (traineeRepository.existsByEmailAndIdNot(newData.getEmail(), id)) {
            throw new RuntimeException("Ya existe otro tutor con ese email");
        }

        if (newData.getCompany() == null || newData.getCompany().getId() == null) {
            throw new RuntimeException("Debes indicar la empresa");
        }

        Optional<Company> companyOptional = companyRepository.findById(newData.getCompany().getId());

        if (companyOptional.isEmpty()) {
            throw new RuntimeException("Empresa no encontrada");
        }

        Trainee trainee = optionalTrainee.get();
        trainee.setFirstName(newData.getFirstName());
        trainee.setLastName(newData.getLastName());
        trainee.setDni(newData.getDni());
        trainee.setEmail(newData.getEmail());
        trainee.setCompany(companyOptional.get());

        return Optional.of(traineeRepository.save(trainee));
    }

    public boolean deleteById(Long id) {
        if (!traineeRepository.existsById(id)) {
            return false;
        }

        traineeRepository.deleteById(id);
        return true;
    }
}
package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.PracticeCreateRequest;
import com.example.documentsAPP.dto.PracticeUpdateRequest;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.Practice;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.model.Trainee;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.repository.PracticeRepository;
import com.example.documentsAPP.repository.StudentRepository;
import com.example.documentsAPP.repository.TraineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PracticeService {

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    public Practice createPractice(PracticeCreateRequest request) {
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un alumno");
        }
        if (request.getCompanyId() == null) {
            throw new RuntimeException("Debe seleccionar una empresa");
        }
        if (request.getTraineeId() == null) {
            throw new RuntimeException("Debe seleccionar un tutor");
        }
        if (request.getTotalHours() == null || request.getTotalHours() <= 0) {
            throw new RuntimeException("Debe indicar las horas totales (> 0)");
        }
        if (request.getDailyHours() == null || request.getDailyHours() <= 0) {
            throw new RuntimeException("Debe indicar las horas diarias (> 0)");
        }
        // Validaciones de los nuevos campos de horas
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Debe indicar la hora de inicio y fin de la práctica");
        }

        Optional<Company> companyOpt = companyRepository.findById(request.getCompanyId());
        if (companyOpt.isEmpty()) {
            throw new RuntimeException("Empresa no encontrada");
        }

        Optional<Trainee> traineeOpt = traineeRepository.findById(request.getTraineeId());
        if (traineeOpt.isEmpty()) {
            throw new RuntimeException("Tutor no encontrado");
        }

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        if (students.isEmpty()) {
            throw new RuntimeException("Alumnos no encontrados");
        }

        Company company = companyOpt.get();

        if (company.getAgreement() == null) {
            throw new RuntimeException("La empresa no tiene convenio asociado");
        }

        Practice practice = new Practice();
        practice.setCompany(company);
        practice.setTrainee(traineeOpt.get());
        practice.setStudents(new HashSet<>(students));

        practice.setAgreementNumber(company.getAgreement().getNumber());
        practice.setAgreementSignDate(company.getAgreement().getSignDate());

        practice.setWorkplace(request.getWorkplace());
        practice.setStartDate(request.getStartDate());
        practice.setEndDate(request.getEndDate());
        practice.setSchedule(request.getSchedule());

        practice.setTotalHours(request.getTotalHours());
        practice.setDailyHours(request.getDailyHours());

        // Seteamos los nuevos datos de hora
        practice.setStartTime(request.getStartTime());
        practice.setEndTime(request.getEndTime());

        return practiceRepository.save(practice);
    }

    public List<Practice> findAll() {
        return practiceRepository.findAll();
    }

    public Practice findById(Long id) {
        return practiceRepository.findById(id).orElse(null);
    }

    public boolean deletePractice(Long id) {
        return practiceRepository.findById(id)
                .map(practice -> {
                    practiceRepository.delete(practice);
                    return true;
                })
                .orElse(false);
    }

    public Practice updatePractice(Long id, PracticeUpdateRequest request) {
        Practice practice = practiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Práctica no encontrada"));

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            if (company.getAgreement() == null) {
                throw new RuntimeException("La empresa no tiene convenio asociado");
            }
            practice.setCompany(company);
            practice.setAgreementNumber(company.getAgreement().getNumber());
            practice.setAgreementSignDate(company.getAgreement().getSignDate());
        }

        if (request.getTraineeId() != null) {
            Trainee trainee = traineeRepository.findById(request.getTraineeId())
                    .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));
            practice.setTrainee(trainee);
        }

        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(request.getStudentIds());
            if (students.isEmpty()) {
                throw new RuntimeException("Alumnos no encontrados");
            }
            practice.setStudents(new HashSet<>(students));
        }

        if (request.getWorkplace() != null) {
            practice.setWorkplace(request.getWorkplace());
        }
        if (request.getStartDate() != null) {
            practice.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            practice.setEndDate(request.getEndDate());
        }
        if (request.getSchedule() != null) {
            practice.setSchedule(request.getSchedule());
        }
        if (request.getTotalHours() != null) {
            practice.setTotalHours(request.getTotalHours());
        }
        if (request.getDailyHours() != null) {
            practice.setDailyHours(request.getDailyHours());
        }
        // Actualizamos los nuevos campos de hora si vienen en la petición
        if (request.getStartTime() != null) {
            practice.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            practice.setEndTime(request.getEndTime());
        }

        return practiceRepository.save(practice);
    }
}
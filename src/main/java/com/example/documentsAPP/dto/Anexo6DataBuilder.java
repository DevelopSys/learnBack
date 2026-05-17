package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.Anexo6Data;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.CoordinatorSchool;
import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.model.LearningResult;
import com.example.documentsAPP.model.Practice;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.model.Trainee;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class Anexo6DataBuilder {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LearningResultService learningResultService;

    public Anexo6DataBuilder(LearningResultService learningResultService) {
        this.learningResultService = learningResultService;
    }

    public Anexo6Data build(Practice practice, Student student) {
        Company company = practice.getCompany();
        Course course = student != null ? student.getCourse() : null;
        CoordinatorSchool coordinator = course != null ? course.getCoordinator() : null;
        Trainee trainee = practice.getTrainee();

        List<LearningResult> learningResults = course != null
                ? learningResultService.findByCourseId(course.getId())
                : List.of();

        List<Anexo6Data.ResultadoFila> resultados = learningResults.stream()
                .map(lr -> Anexo6Data.ResultadoFila.builder()
                        .asignaturaNombre(n(lr.getSubjectName()))
                        .raCodigo(n(lr.getSubjectCode()))
                        .raNumero(lr.getNumber() != null ? String.valueOf(lr.getNumber()) : "")
                        .build())
                .toList();

        return Anexo6Data.builder()
                .empresaNombre(company != null ? n(company.getLegalName()) : "")
                .empresaNif(company != null ? n(company.getNif()) : "")
                .empresaTelefono(company != null ? n(company.getPhone()) : "")
                .entidadCorreo(trainee != null ? n(trainee.getEmail()) : "")

                .alumnoNombre(student != null ? n(student.getFirstName()) : "")
                .alumnoApellidos(student != null ? n(student.getLastName()) : "")
                .alumnoCorreo(student != null ? n(student.getEmail()) : "") // <--- ¡AÑADIDO!
                .alumnoTelefono("") // Asumo que no lo tienes en el backend, por eso queda vacío

                .centroNombre(course != null ? n(course.getName()) : "")
                .centroCorreo(coordinator != null ? n(coordinator.getEmail()) : "")
                .centroTelefono("")

                .centroTutorNombre(coordinator != null ? n(coordinator.getName()) : "")
                .centroTutorCorreo(coordinator != null ? n(coordinator.getEmail()) : "")

                .entidadTutorNombre(trainee != null
                        ? (n(trainee.getFirstName()) + " " + n(trainee.getLastName())).trim()
                        : "")
                .entidadTutorCorreo(trainee != null ? n(trainee.getEmail()) : "")

                .cicloNombre(course != null ? n(course.getName()) : "")
                .cursoCodigo(course != null ? n(course.getCode()) : "")

                .fechaInicio(practice.getStartDate() != null ? practice.getStartDate().format(FECHA) : "")
                .fechaFin(practice.getEndDate() != null ? practice.getEndDate().format(FECHA) : "")
                .horaInicio(practice.getStartTime() != null ? practice.getStartTime().toString() : "")
                .horaFin(practice.getEndTime() != null ? practice.getEndTime().toString() : "")
                .horasTotales(practice.getTotalHours() != null ? String.valueOf(practice.getTotalHours()) : "")
                .fechaFirma(LocalDate.now().format(FECHA))

                .resultados(resultados)
                .build();
    }

    private String n(String s) {
        return s == null ? "" : s;
    }
}
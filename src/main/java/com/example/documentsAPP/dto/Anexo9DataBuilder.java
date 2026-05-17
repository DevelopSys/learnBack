package com.example.documentsAPP.dto;

import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.model.InfoCourse;
import com.example.documentsAPP.model.LearningResult;
import com.example.documentsAPP.model.Practice;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.model.Trainee;
import com.example.documentsAPP.service.InfoCourseService;
import com.example.documentsAPP.service.LearningResultService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class Anexo9DataBuilder {

    private final LearningResultService learningResultService;
    private final InfoCourseService infoCourseService;

    public Anexo9DataBuilder(LearningResultService learningResultService,
                             InfoCourseService infoCourseService) {
        this.learningResultService = learningResultService;
        this.infoCourseService = infoCourseService;
    }

    public Anexo9Data build(Practice practice, Student student) {
        // InfoCourse para el año del curso
        List<InfoCourse> infos = infoCourseService.findAll();
        InfoCourse info = infos.isEmpty() ? new InfoCourse() : infos.get(0);
        String cursoAnio = info.getSchoolYear() != null ? info.getSchoolYear() : "";

        Company company = practice.getCompany();
        Course course = student != null ? student.getCourse() : null;
        Trainee trainee = practice.getTrainee();

        // Resultados de aprendizaje
        List<LearningResult> learningResults = course != null
                ? learningResultService.findByCourseId(course.getId())
                : List.of();

        List<Anexo9Data.ResultadoFila> resultados = learningResults.stream()
                .map(lr -> Anexo9Data.ResultadoFila.builder()
                        .codAsignatura(n(lr.getSubjectCode()))
                        .resultadoNumero(lr.getNumber() != null ? String.valueOf(lr.getNumber()) : "")
                        .build())
                .toList();

        // Período de formación: X solo en el curso del alumno
        int nivel = (course != null && course.getLevel() != 0) ? course.getLevel() : 0;
        String c1 = nivel == 1 ? "X" : "";
        String c2 = nivel == 2 ? "X" : "";
        String c3 = nivel == 3 ? "X" : "";

        // Fecha actual para la firma
        LocalDate now = LocalDate.now();
        String dia = String.valueOf(now.getDayOfMonth());
        String mes = now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(now.getYear());

        return Anexo9Data.builder()
                .cursoAnio(cursoAnio)
                .convenioNumero(n(practice.getAgreementNumber()))

                .alumnoNombre(student != null ? n(student.getFirstName()) : "")
                .alumnoApellido(student != null ? n(student.getLastName()) : "")
                .alumnoCorreo(student != null ? n(student.getEmail()) : "")

                .entidadNombre(company != null ? n(company.getLegalName()) : "")
                .entidadTutorNombre(trainee != null ? n(trainee.getFirstName()) : "")
                .entidadTutorApellido(trainee != null ? n(trainee.getLastName()) : "")
                .entidadTutorCorreo(trainee != null ? n(trainee.getEmail()) : "")

                .cursoNombre(course != null ? n(course.getName()) : "")
                .cursoCodigo(course != null ? n(course.getCode()) : "")
                .practicasHoras(practice.getTotalHours() != null ? String.valueOf(practice.getTotalHours()) : "")

                .c1(c1)
                .c2(c2)
                .c3(c3)

                .dia(dia)
                .mes(mes)
                .anio(anio)

                .resultados(resultados)
                .build();
    }

    private String n(String s) {
        return s == null ? "" : s;
    }
}
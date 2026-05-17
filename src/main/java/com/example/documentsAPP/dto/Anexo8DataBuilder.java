package com.example.documentsAPP.dto;

import com.example.documentsAPP.dto.Anexo8Data;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class Anexo8DataBuilder {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InfoCourseService infoCourseService;
    private final LearningResultService learningResultService;

    public Anexo8DataBuilder(InfoCourseService infoCourseService,
                             LearningResultService learningResultService) {
        this.infoCourseService = infoCourseService;
        this.learningResultService = learningResultService;
    }

    public Anexo8Data build(Practice practice, Student student) {
        List<InfoCourse> infos = infoCourseService.findAll();
        InfoCourse info = infos.isEmpty() ? new InfoCourse() : infos.get(0);

        Company company = practice.getCompany();
        Course course = student != null ? student.getCourse() : null;
        Trainee trainee = practice.getTrainee();

        // Resultados de aprendizaje del ciclo
        List<LearningResult> learningResults = course != null
                ? learningResultService.findByCourseId(course.getId())
                : List.of();

        List<Anexo8Data.ResultadoFila> resultados = learningResults.stream()
                .map(lr -> Anexo8Data.ResultadoFila.builder()
                        .codigoModulo(n(lr.getSubjectCode()))
                        .raNumero(lr.getNumber() != null ? String.valueOf(lr.getNumber()) : "")
                        .build())
                .toList();

        // Periodos quincenales — Implementación A
        List<Anexo8Data.PeriodoSeguimiento> periodos =
                calcularPeriodos(practice.getStartDate(), practice.getEndDate());

        return Anexo8Data.builder()
                .cursoAnio(info.getSchoolYear() != null ? info.getSchoolYear() : "")
                .convenioNumero(n(practice.getAgreementNumber()))

                .alumnoApellidos(student != null ? n(student.getLastName()) : "")
                .alumnoNombre(student != null ? n(student.getFirstName()) : "")
                .alumnoCorreo(student != null ? n(student.getEmail()) : "")

                .entidadNombre(company != null ? n(company.getLegalName()) : "")
                .entidadTutorApellidos(trainee != null ? n(trainee.getLastName()) : "")
                .entidadTutorNombre(trainee != null ? n(trainee.getFirstName()) : "")
                .entidadTutorCorreo(trainee != null ? n(trainee.getEmail()) : "")

                .periodos(periodos)
                .resultados(resultados)
                .build();
    }

    /**
     * Implementación A:
     *  - Hoja 1 : desde startDate hasta el domingo de la semana siguiente (cierra el ciclo)
     *  - Hoja N : lunes siguiente → domingo de dos semanas después
     *
     * Ejemplo con inicio jueves 09/01/2026:
     *   Hoja 1  → 09/01/2026 – 18/01/2026  (domingo de la semana siguiente)
     *   Hoja 2  → 19/01/2026 – 01/02/2026  (lunes, 2 semanas completas)
     *   Hoja 3  → 02/02/2026 – 15/02/2026
     *   ...
     */
    private List<Anexo8Data.PeriodoSeguimiento> calcularPeriodos(LocalDate inicio, LocalDate fin) {
        List<Anexo8Data.PeriodoSeguimiento> lista = new ArrayList<>();
        if (inicio == null || fin == null || inicio.isAfter(fin)) return lista;

        // Primera hoja: desde el inicio real hasta el próximo domingo
        // Si ya es domingo, avanzamos al siguiente (siempre cierra en el domingo de la semana siguiente)
        LocalDate primerDomingo = inicio.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate primerFin = primerDomingo.isBefore(fin) ? primerDomingo : fin;

        lista.add(Anexo8Data.PeriodoSeguimiento.builder()
                .fechaInicio(inicio.format(FECHA))
                .fechaFin(primerFin.format(FECHA))
                .build());

        // Bloques siguientes: lunes → domingo de dos semanas completas
        LocalDate bloqueInicio = primerFin.plusDays(1); // primer lunes

        while (!bloqueInicio.isAfter(fin)) {
            // Domingo de dentro de dos semanas = domingo de la semana actual + 7 días
            LocalDate domingoDeSemanaActual = bloqueInicio.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate bloqueFin = domingoDeSemanaActual.plusWeeks(1);

            if (bloqueFin.isAfter(fin)) bloqueFin = fin;

            lista.add(Anexo8Data.PeriodoSeguimiento.builder()
                    .fechaInicio(bloqueInicio.format(FECHA))
                    .fechaFin(bloqueFin.format(FECHA))
                    .build());

            bloqueInicio = bloqueFin.plusDays(1);
        }

        return lista;
    }

    private String n(String s) {
        return s == null ? "" : s;
    }
}
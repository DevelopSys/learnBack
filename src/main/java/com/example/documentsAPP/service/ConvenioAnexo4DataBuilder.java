package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.ConvenioAnexo4Data;
import com.example.documentsAPP.model.CoordinatorSchool;
import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.model.Practice;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.model.InfoCourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ConvenioAnexo4DataBuilder {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private InfoCourseService infoCourseService;

    public ConvenioAnexo4Data build(Practice p) {
        // Cargar configuración global para el año del curso
        List<InfoCourse> infos = infoCourseService.findAll();
        InfoCourse info = infos.isEmpty() ? new InfoCourse() : infos.get(0);
        String cursoAnio = info.getSchoolYear() != null ? info.getSchoolYear() : "";

        // Nº y fecha de convenio desde la práctica
        String convenioNumero = n(p.getAgreementNumber());
        String convenioFecha = p.getAgreementSignDate() != null ? p.getAgreementSignDate().format(FECHA) : "";

        // Tutor del centro (CoordinatorSchool asociado al Course)
        Course course = null;
        if (p.getStudents() != null && !p.getStudents().isEmpty()) {
            Student first = p.getStudents().iterator().next();
            course = first.getCourse();
        }
        CoordinatorSchool coord = course != null ? course.getCoordinator() : null;

        String centroNombre = course != null ? n(course.getName()) : "";
        String centroTutorApellido = coord != null ? extraerApellidos(coord.getName()) : "";
        String centroTutorNombre = coord != null ? extraerNombre(coord.getName()) : "";
        String centroTutorDni = coord != null ? n(coord.getDni()) : "";
        String centroTutorMail = coord != null ? n(coord.getEmail()) : "";

        // Tutor de empresa (Trainee)
        String entidadTutorApellido = p.getTrainee() != null ? n(p.getTrainee().getLastName()) : "";
        String entidadTutorNombre = p.getTrainee() != null ? n(p.getTrainee().getFirstName()) : "";
        String entidadTutorDni = p.getTrainee() != null ? n(p.getTrainee().getDni()) : "";
        String entidadTutorMail = p.getTrainee() != null ? n(p.getTrainee().getEmail()) : "";

        // Datos de curso (desde el primer alumno)
        String cursoNombre = "";
        String cursoNivel = "";
        String cursoCodigo = "";
        if (p.getStudents() != null && !p.getStudents().isEmpty()) {
            Student s0 = p.getStudents().iterator().next();
            if (s0.getCourse() != null) {
                cursoNombre = n(s0.getCourse().getName());
                cursoNivel = Integer.toString(s0.getCourse().getLevel());
                cursoCodigo = n(s0.getCourse().getCode());
            }
        }

        // Lista de alumnos ordenados por apellidos/nombre
        AtomicInteger contador = new AtomicInteger(1);
        List<ConvenioAnexo4Data.AlumnoFila> alumnos = p.getStudents().stream()
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName))
                .map(s -> {
                    String num = String.format("%02d", contador.getAndIncrement());
                    String ape = n(s.getLastName());
                    String nom = n(s.getFirstName());

                    return ConvenioAnexo4Data.AlumnoFila.builder()
                            .numero(num)
                            .alumnoApellido(ape)
                            .alumnoNombre(nom)
                            .dni(n(s.getDni()))
                            .fechaNacimiento(s.getBirthDate() != null ? s.getBirthDate().format(FECHA) : "")
                            .practicaInicio(p.getStartDate() != null ? p.getStartDate().format(FECHA) : "")
                            .practicaFin(p.getEndDate() != null ? p.getEndDate().format(FECHA) : "")
                            .practicaDias(n(p.getSchedule()))
                            .practicaHoraInicio(p.getStartTime() != null ? p.getStartTime().toString() : "")
                            .practicaHoraFin(p.getEndTime() != null ? p.getEndTime().toString() : "")
                            .practicaHorasSemana(p.getDailyHours() != null ? p.getDailyHours().toString() : "")
                            .practicaHorasTotal(p.getTotalHours() != null ? p.getTotalHours().toString() : "")
                            .build();
                })
                .toList();

        return ConvenioAnexo4Data.builder()
                .cursoAnio(cursoAnio)
                .convenioNumero(convenioNumero)
                .convenioFecha(convenioFecha)
                .centroNombre(centroNombre)
                .entidadNombre(p.getCompany() != null ? n(p.getCompany().getLegalName()) : "")
                .centroTutorApellido(centroTutorApellido)
                .centroTutorNombre(centroTutorNombre)
                .centroTutorDni(centroTutorDni)
                .centroTutorMail(centroTutorMail)
                .entidadTutorApellido(entidadTutorApellido)
                .entidadTutorNombre(entidadTutorNombre)
                .entidadTutorDni(entidadTutorDni)
                .entidadTutorMail(entidadTutorMail)
                .entidadDomicilioTrabajo(n(p.getWorkplace()))
                .cursoNombre(cursoNombre)
                .cursoNivel(cursoNivel)
                .cursoCodigo(cursoCodigo)
                .alumnos(alumnos)
                .fechaFirma(java.time.LocalDate.now().format(FECHA)) // <- Fecha actual al generar el documento
                .build();
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    private String extraerApellidos(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        String[] trozos = nombreCompleto.trim().split("\\s+");
        if (trozos.length <= 1) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < trozos.length; i++) {
            if (i > 1) sb.append(" ");
            sb.append(trozos[i]);
        }
        return sb.toString();
    }

    private String extraerNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        String[] trozos = nombreCompleto.trim().split("\\s+");
        return trozos[0];
    }
}
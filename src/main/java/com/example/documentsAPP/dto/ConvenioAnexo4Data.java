package com.example.documentsAPP.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ConvenioAnexo4Data {

    // Cabecera y texto
    String cursoAnio;          // {{CURSO_ANIO}}
    String convenioNumero;     // {{CONVENIO_NUMERO}}
    String convenioFecha;      // {{CONVENIO_FECHA}}
    String centroNombre;       // {{CENTRO_NOMBRE}}
    String entidadNombre;      // {{ENTIDAD_NOMBRE}}

    // Tutor centro
    String centroTutorApellido;  // {{CENTRO_TUTOR_APELLIDO}}
    String centroTutorNombre;    // {{CENTRO_TUTOR_NOMBRE}}
    String centroTutorDni;       // {{CENTRO_TUTOR_DNI}}
    String centroTutorMail;      // {{CENTRO_TUTOR_MAIL}}

    // Tutor empresa (ojo con los placeholders ENTIIDAD_ en la plantilla)
    String entidadTutorApellido; // {{ENTIDAD_TUTOR_APELLIDO}}
    String entidadTutorNombre;   // {{ENTIIDAD_TUTOR_NOMBRE}} o {{ENTIDAD_TUTOR_NOMBRE}}
    String entidadTutorDni;      // {{ENTIIDAD_TUTOR_DNI}} o {{ENTIDAD_TUTOR_DNI}}
    String entidadTutorMail;     // {{ENTIIDAD_TUTOR_MAIL}} o {{ENTIDAD_TUTOR_MAIL}}

    // Domicilio del centro de trabajo
    String entidadDomicilioTrabajo; // {{ENTIIDAD_DOMICILIO_TRABAJO}} o {{ENTIDAD_DOMICILIO_TRABAJO}}

    // Datos estudios
    String cursoNombre;        // {{CURSO_NOMBRE}}
    String cursoNivel;         // {{CURSO_NIVEL}}
    String cursoCodigo;        // {{CURSO_CODIGO}}

    // Lista de alumnos
    List<AlumnoFila> alumnos;

    // Firma
    String fechaFirma;         // {{FECHA_FIRMA}}

    @Value
    @Builder
    public static class AlumnoFila {
        String numero;              // Nº fila (01,02,03,04...)
        String alumnoApellido;      // {{ALUMNO_APELLIDO}}
        String alumnoNombre;        // {{ALUMNO_NOMBRE}}
        String dni;                 // {{ALUMNO_DNI}}
        String fechaNacimiento;     // {{ALUMNO_FECHA}}
        String practicaInicio;      // {{PRACTICA_INICIO}}
        String practicaFin;         // {{PRACTICA_FIN}}
        String practicaDias;        // {{PRACTICA_DIAS}}
        String practicaHoraInicio;  // {{PRACTICA_HORA_INICIO}}
        String practicaHoraFin;     // {{PRACTICA_HORA_FIN}  (sin segunda llave si así está en el docx)
        String practicaHorasSemana; // {{PRACTICA_HORA_SEMANA}}
        String practicaHorasTotal;  // {{PRACTICA_HORAS_TOTAL}}
    }
}
package com.example.documentsAPP.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anexo8Data {

    private String cursoAnio;          // {{CURSO_ANIO}}
    private String convenioNumero;     // {{CONVENIO_NUMERO}}

    private String alumnoApellidos;    // {{ALUMNO_APELLIDOS}}
    private String alumnoNombre;       // {{ALUMNO_NOMBRE}}
    private String alumnoCorreo;       // {{ALUMNO_CORREO}}

    private String entidadNombre;      // {{ENTIDAD_NOMBRE}}
    private String entidadTutorApellidos; // {{ENTIDAD_TUTOR_APELLIDOS}}
    private String entidadTutorNombre;    // {{ENTIDAD_TUTOR_NOMBRE}}
    private String entidadTutorCorreo;   // {{ENTIDAD_TUTOR_CORREO}}

    // Una entrada por cada bloque quincenal → una página en el DOCX
    private List<PeriodoSeguimiento> periodos;

    // Filas de la tabla de resultados de aprendizaje (se repiten en cada página)
    private List<ResultadoFila> resultados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodoSeguimiento {
        private String fechaInicio;   // {{FECHA_INICIO}}
        private String fechaFin;      // {{FECHA_FIN}}
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoFila {
        private String codigoModulo;  // {{CODIGO_MODULO}}
        private String raNumero;      // {{RA_NUMERO}}
    }
}
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
public class Anexo9Data {

    private String cursoAnio;       // {{CURSO_ANIO}}
    private String convenioNumero;  // {{CONVENIO_NUMERO}}

    private String alumnoApellido;
    private String alumnoNombre;
    private String alumnoCorreo;

    private String entidadNombre;
    private String entidadTutorApellido;
    private String entidadTutorNombre;
    private String entidadTutorCorreo;

    private String cursoNombre;
    private String cursoCodigo;
    private String practicasHoras;

    // Periodo de formación: solo el del curso del alumno tendrá "X", el resto ""
    private String c1;  // {{C_1}}
    private String c2;  // {{C_2}}
    private String c3;  // {{C_3}}

    private String dia;
    private String mes;
    private String anio;

    private List<ResultadoFila> resultados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoFila {
        private String codAsignatura;
        private String resultadoNumero;
    }
}
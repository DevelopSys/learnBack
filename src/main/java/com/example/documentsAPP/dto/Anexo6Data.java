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
public class Anexo6Data {

    private String empresaNombre;
    private String empresaNif;
    private String empresaTelefono;
    private String entidadCorreo;

    private String alumnoNombre;
    private String alumnoApellidos;
    private String alumnoCorreo;
    private String alumnoTelefono;

    private String centroNombre;
    private String centroCorreo;
    private String centroTelefono;

    private String centroTutorNombre;
    private String centroTutorCorreo;

    private String entidadTutorNombre;
    private String entidadTutorCorreo;

    private String cicloNombre;
    private String cursoCodigo;

    private String fechaInicio;
    private String fechaFin;
    private String horaInicio;
    private String horaFin;
    private String horasTotales;
    private String fechaFirma;

    private List<ResultadoFila> resultados;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoFila {
        private String asignaturaNombre;
        private String raCodigo;
        private String raNumero;
    }
}
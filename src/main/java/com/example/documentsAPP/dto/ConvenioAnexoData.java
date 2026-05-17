package com.example.documentsAPP.dto;

public record ConvenioAnexoData(
        // Convenio
        String convenioNumero,
        String convenioLugar,
        String convenioDia,
        String convenioMes,
        String convenioAnio,

        // Centro Docente
        String centroNombre,
        String centroNif,
        String centroCodigo,
        String centroLocalidad,
        String centroCalle,
        String centroCp,
        String centroProvincia,
        String centroPais,
        String centroTitularNombre,
        String centroTitularNif,
        String centroTitularCargo,

        // Entidad colaboradora (Company)
        String entidadNombre,
        String entidadNif,
        String entidadActividad,
        String entidadCalle,
        String entidadCp,
        String entidadLocalidad,
        String entidadProvincia,
        String entidadPais,

        // Representante 1
        String rep1Nombre,
        String rep1Cargo,
        String rep1BaseNormativa,

        // Representante 2 (opcional)
        String rep2Nombre,
        String rep2Cargo,
        String rep2NombradoPor,
        String rep2BaseNormativa
) {}
package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.ConvenioAnexoData;
import com.example.documentsAPP.model.Agreement;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.InfoCourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ConvenioAnexoDataBuilder {

    @Autowired
    private InfoCourseService infoCourseService;

    public ConvenioAnexoData build(Company company, Agreement agreement) {
        // Cargar información del centro
        List<InfoCourse> infos = infoCourseService.findAll();
        InfoCourse info = infos.isEmpty() ? new InfoCourse() : infos.get(0);

        String rep1Nombre = "";
        String rep2Nombre = "";

        if (company.getRepresentatives() != null && !company.getRepresentatives().isEmpty()) {
            rep1Nombre = company.getRepresentatives().get(0).getFullName();
            if (company.getRepresentatives().size() > 1) {
                rep2Nombre = company.getRepresentatives().get(1).getFullName();
            }
        }

        // --- Procesamiento de la Fecha de Firma ---
        String dia = "";
        String mes = "";
        String anio = "";

        if (agreement.getSignDate() != null) {
            try {
                // Si getSignDate() devuelve LocalDate, quita el toString() y el parse.
                // Si devuelve String (ej: "2026-05-16"), esto lo procesará correctamente.
                LocalDate fecha = LocalDate.parse(agreement.getSignDate().toString());
                dia = String.valueOf(fecha.getDayOfMonth());
                String[] nombresMeses = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
                        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
                mes = nombresMeses[fecha.getMonthValue() - 1];
                anio = String.valueOf(fecha.getYear());
            } catch (DateTimeParseException e) {
                // Fallback por si la fecha no viene en formato estándar
                dia = "___"; mes = "___"; anio = "___";
            }
        }

        return new ConvenioAnexoData(
                // Convenio
                agreement.getNumber() != null ? agreement.getNumber() : "",
                info.getSchoolCity() != null ? info.getSchoolCity() : "", // Lugar de la firma = ciudad del centro
                dia,
                mes,
                anio,

                // Centro Docente (Mapeando desde InfoCourse)
                info.getSchoolName() != null ? info.getSchoolName() : "",
                info.getDirectorNif() != null ? info.getDirectorNif() : "", // Usando NIF del director por ahora o ajusta si creas school_nif
                info.getSchoolNumber() != null ? info.getSchoolNumber() : "",
                info.getSchoolLocal() != null ? info.getSchoolLocal() : "",
                info.getSchoolAddress() != null ? info.getSchoolAddress() : "",
                info.getSchoolPostalCode() != null ? info.getSchoolPostalCode() : "",
                info.getSchoolState() != null ? info.getSchoolState() : "",
                "España", // centroPais
                (info.getDirectorName() != null ? info.getDirectorName() : "") + " " +
                        (info.getDirectorLastName() != null ? info.getDirectorLastName() : ""),
                info.getDirectorNif() != null ? info.getDirectorNif() : "",
                "Director/a", // centroTitularCargo

                // Entidad colaboradora (Company)
                company.getLegalName() != null ? company.getLegalName() : "",
                company.getNif() != null ? company.getNif() : "",
                company.getActivity() != null ? company.getActivity() : "",
                company.getStreet() != null ? company.getStreet() : "",
                company.getPostalCode() != null ? company.getPostalCode() : "",
                company.getCity() != null ? company.getCity() : "",
                company.getProvince() != null ? company.getProvince() : "",
                company.getCountry() != null ? company.getCountry() : "",

                // Representante 1
                rep1Nombre,
                "Representante Legal", // rep1Cargo
                "Poderes de representación", // rep1BaseNormativa

                // Representante 2
                rep2Nombre,
                "Representante Legal", // rep2Cargo
                "", // rep2NombradoPor
                ""  // rep2BaseNormativa
        );
    }
}
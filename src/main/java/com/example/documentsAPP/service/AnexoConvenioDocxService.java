package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.ConvenioAnexoData;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnexoConvenioDocxService {

    private static final String TEMPLATE_PATH = "/templates/1_Anexo.docx";

    public byte[] generarAnexo(ConvenioAnexoData data) {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
             XWPFDocument doc = new XWPFDocument(is)) {

            Map<String, String> values = buildPlaceholderMap(data);

            // Reemplazo en párrafos sueltos
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraphPreservingFormat(p, values);
            }

            // Reemplazo en tablas
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            replaceInParagraphPreservingFormat(p, values);
                        }
                    }
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando anexo de convenio", e);
        }
    }

    private Map<String, String> buildPlaceholderMap(ConvenioAnexoData d) {
        Map<String, String> m = new HashMap<>();

        // Convenio
        m.put("{{CONVENIO_NUMERO}}", n(d.convenioNumero()));
        m.put("{{CONVENIO_LUGAR}}", n(d.convenioLugar()));
        m.put("{{DIA}}", n(d.convenioDia()));
        m.put("{{MES}}", n(d.convenioMes()));
        m.put("{{ANIO}}", n(d.convenioAnio()));

        // Centro docente
        m.put("{{CENTRO_NOMBRE}}", n(d.centroNombre()));
        m.put("{{CENTRO_NIF}}", n(d.centroNif()));
        m.put("{{CENTRO_CODIGO}}", n(d.centroCodigo()));
        m.put("{{CENTRO_LOCALIDAD}}", n(d.centroLocalidad()));
        m.put("{{CENTRO_CALLE}}", n(d.centroCalle()));
        m.put("{{CENTRO_CP}}", n(d.centroCp()));
        m.put("{{CENTRO_PROVINCIA}}", n(d.centroProvincia()));
        m.put("{{CENTRO_PAIS}}", n(d.centroPais()));
        m.put("{{CENTRO_TITULAR_NOMBRE}}", n(d.centroTitularNombre()));
        m.put("{{CENTRO_TITULAR_NIF}}", n(d.centroTitularNif()));
        m.put("{{CENTRO_TITULAR_CARGO}}", n(d.centroTitularCargo()));

        // Entidad colaboradora
        m.put("{{ENTIDAD_NOMBRE}}", n(d.entidadNombre()));
        m.put("{{ENTIDAD_NIF}}", n(d.entidadNif()));
        m.put("{{ENTIDAD_ACTIVIDAD}}", n(d.entidadActividad()));
        m.put("{{ENTIDAD_CALLE}}", n(d.entidadCalle()));
        m.put("{{ENTIDAD_CP}}", n(d.entidadCp()));
        m.put("{{ENTIDAD_LOCALIDAD}}", n(d.entidadLocalidad()));
        m.put("{{ENTIDAD_PROVINCIA}}", n(d.entidadProvincia()));
        m.put("{{ENTIDAD_PAIS}}", n(d.entidadPais()));

        // Representante 1
        m.put("{{REP1_NOMBRE}}", n(d.rep1Nombre()));
        m.put("{{REP1_CARGO}}", n(d.rep1Cargo()));
        m.put("{{REP1_BASE_NORMATIVA}}", n(d.rep1BaseNormativa()));

        // Representante 2
        m.put("{{REP2_NOMBRE}}", n(d.rep2Nombre()));
        m.put("{{REP2_CARGO}}", n(d.rep2Cargo()));
        m.put("{{REP2_NOMBRADO_POR}}", n(d.rep2NombradoPor()));
        m.put("{{REP2_BASE_NORMATIVA}}", n(d.rep2BaseNormativa()));

        return m;
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    /**
     * Reemplaza variables respetando la división de Runs (formato original del documento)
     */
    private void replaceInParagraphPreservingFormat(XWPFParagraph paragraph, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = entry.getKey();
            String replacement = entry.getValue();

            while (true) {
                String text = paragraph.getText();
                if (!text.contains(placeholder)) break;

                int placeholderStart = text.indexOf(placeholder);
                int placeholderEnd = placeholderStart + placeholder.length();

                int startRunIndex = -1;
                int endRunIndex = -1;
                int posInStartRun = -1;
                int posInEndRun = -1;

                int currentPos = 0;
                List<XWPFRun> runs = paragraph.getRuns();

                // Identificar en qué Run(s) empieza y termina el placeholder
                for (int i = 0; i < runs.size(); i++) {
                    String runText = runs.get(i).getText(0);
                    if (runText == null) continue;

                    int runStart = currentPos;
                    int runEnd = currentPos + runText.length();

                    if (startRunIndex == -1 && placeholderStart >= runStart && placeholderStart < runEnd) {
                        startRunIndex = i;
                        posInStartRun = placeholderStart - runStart;
                    }
                    if (endRunIndex == -1 && placeholderEnd > runStart && placeholderEnd <= runEnd) {
                        endRunIndex = i;
                        posInEndRun = placeholderEnd - runStart;
                    }
                    currentPos = runEnd;
                }

                if (startRunIndex == -1 || endRunIndex == -1) break;

                // Si todo el placeholder está en un solo Run
                if (startRunIndex == endRunIndex) {
                    XWPFRun run = runs.get(startRunIndex);
                    String runText = run.getText(0);
                    run.setText(runText.substring(0, posInStartRun) + replacement + runText.substring(posInEndRun), 0);
                } else {
                    // Si el placeholder está dividido en varios Runs (Word hace esto frecuentemente)
                    XWPFRun startRun = runs.get(startRunIndex);
                    String startRunText = startRun.getText(0);
                    startRun.setText(startRunText.substring(0, posInStartRun) + replacement, 0);

                    // Vaciar los runs intermedios
                    for (int i = startRunIndex + 1; i < endRunIndex; i++) {
                        runs.get(i).setText("", 0);
                    }

                    // Ajustar el run final
                    XWPFRun endRun = runs.get(endRunIndex);
                    String endRunText = endRun.getText(0);
                    endRun.setText(endRunText.substring(posInEndRun), 0);
                }
            }
        }
    }
}
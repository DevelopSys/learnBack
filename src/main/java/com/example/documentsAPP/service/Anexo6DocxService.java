package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.Anexo6Data;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Anexo6DocxService {

    private static final String TEMPLATE_PATH = "/templates/6_Anexo.docx";

    public byte[] generarAnexo6(Anexo6Data data) {
        InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IllegalStateException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        try (InputStream streamCerrado = is;
             XWPFDocument doc = new XWPFDocument(streamCerrado)) {

            Map<String, String> values = buildPlaceholderMap(data);

            // 1. Párrafos sueltos del documento
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraph(p, values);
            }

            // 2. Tablas (recursivo para procesar tablas anidadas)
            for (XWPFTable table : doc.getTables()) {
                procesarTablaRecursiva(table, values);
            }

            // 3. Tabla de resultados de aprendizaje
            fillResultadosTable(doc, data);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Error generando anexo 6", e);
        }
    }

    private void procesarTablaRecursiva(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                // Párrafos normales de la celda
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, values);
                }
                // Si la celda tiene tablas dentro, llamada recursiva
                for (XWPFTable nestedTable : cell.getTables()) {
                    procesarTablaRecursiva(nestedTable, values);
                }
            }
        }
    }

    private Map<String, String> buildPlaceholderMap(Anexo6Data d) {
        Map<String, String> m = new HashMap<>();

        putAll(m, "FECHA_ANEXO", n(d.getFechaFirma()));

        putAll(m, "CODIGO_CURSO", n(d.getCursoCodigo()));
        putAll(m, "NOMBRE_CURSO", n(d.getCicloNombre()));

        String nombreCompleto = (n(d.getAlumnoNombre()) + " " + n(d.getAlumnoApellidos())).trim();
        putAll(m, "ALUMNO_NOMBRE", nombreCompleto);
        putAll(m, "ALUMNO_CORREO", n(d.getAlumnoCorreo()));
        putAll(m, "ALUMNO_TELEFONO", n(d.getAlumnoTelefono()));

        putAll(m, "CENTO_NOMBRE", n(d.getCentroNombre()));
        putAll(m, "CENTRO_NOMBRE", n(d.getCentroNombre()));
        putAll(m, "CENTRO_CORREO", n(d.getCentroCorreo()));
        putAll(m, "CENTRO_TELEFONO", n(d.getCentroTelefono()));

        putAll(m, "CENTRO_TUTOR_NOMBRE", n(d.getCentroTutorNombre()));
        putAll(m, "CENTRO_TUTOR_CORREO", n(d.getCentroTutorCorreo()));

        putAll(m, "ENTIDAD_NOMBRE", n(d.getEmpresaNombre()));
        putAll(m, "ENTIDAD_NIF", n(d.getEmpresaNif()));
        putAll(m, "ENTIDAD_CORREO", n(d.getEntidadCorreo()));
        putAll(m, "ENTIDAD_TELEFONO", n(d.getEmpresaTelefono()));

        putAll(m, "ENTIDAD_TUTOR_NOMBRE", n(d.getEntidadTutorNombre()));
        putAll(m, "ENTIDAD_TUTOR_CORREO", n(d.getEntidadTutorCorreo()));

        putAll(m, "FECHA_INICIO", n(d.getFechaInicio()));
        putAll(m, "FECHA_FIN", n(d.getFechaFin()));
        putAll(m, "HORA_INICIO", n(d.getHoraInicio()));
        putAll(m, "HORA_FIN", n(d.getHoraFin()));
        putAll(m, "HORAS_TOTAL", n(d.getHorasTotales()));

        return m;
    }

    private void putAll(Map<String, String> map, String baseKey, String value) {
        map.put(baseKey, value);
        map.put("{{" + baseKey + "}}", value);
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String t = run.getText(0);
            fullText.append(t == null ? "" : t);
        }

        String texto = fullText.toString();

        boolean hayReemplazo = false;
        for (String key : values.keySet()) {
            if (texto.contains(key)) {
                hayReemplazo = true;
                break;
            }
        }

        if (!hayReemplazo) return;

        // ──────────────────────────────────────────────────────────
        // SOLUCIÓN LLAVES: Ordenamos las claves de mayor a menor longitud
        // Así reemplaza {{CLAVE}} ANTES que CLAVE.
        // ──────────────────────────────────────────────────────────
        List<String> keys = new ArrayList<>(values.keySet());
        keys.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String key : keys) {
            texto = texto.replace(key, values.get(key));
        }

        for (int i = 0; i < runs.size(); i++) {
            runs.get(i).setText(i == 0 ? texto : "", 0);
        }
    }

    private void fillResultadosTable(XWPFDocument doc, Anexo6Data data) {
        if (data.getResultados() == null || data.getResultados().isEmpty()) return;

        for (XWPFTable table : doc.getTables()) {
            XWPFTableRow plantilla = null;
            int plantillaIndex = -1;

            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText();
                    if (cellText != null &&
                            (cellText.contains("{{ASIGNATURA_NOMBRE}}")
                                    || cellText.contains("ASIGNATURA_NOMBRE")
                                    || cellText.contains("{{RA_CODIGO}}")
                                    || cellText.contains("RA_CODIGO")
                                    || cellText.contains("{{RA_NUMERO}}")
                                    || cellText.contains("RA_NUMERO"))) {
                        plantilla = row;
                        plantillaIndex = i;
                        break;
                    }
                }
                if (plantilla != null) break;
            }

            if (plantilla == null) continue;

            List<List<List<String>>> textoOriginal = new ArrayList<>();
            for (XWPFTableCell cell : plantilla.getTableCells()) {
                List<List<String>> cellTexts = new ArrayList<>();
                for (XWPFParagraph p : cell.getParagraphs()) {
                    List<String> runTexts = new ArrayList<>();
                    for (XWPFRun run : p.getRuns()) {
                        String t = run.getText(0);
                        runTexts.add(t == null ? "" : t);
                    }
                    cellTexts.add(runTexts);
                }
                textoOriginal.add(cellTexts);
            }

            for (int idx = 0; idx < data.getResultados().size(); idx++) {
                Anexo6Data.ResultadoFila ra = data.getResultados().get(idx);
                XWPFTableRow currentRow;

                if (idx == 0) {
                    currentRow = plantilla;
                } else {
                    currentRow = table.insertNewTableRow(plantillaIndex + idx);
                    copyRowStructure(plantilla, currentRow);

                    List<XWPFTableCell> cells = currentRow.getTableCells();
                    for (int ci = 0; ci < cells.size() && ci < textoOriginal.size(); ci++) {
                        List<XWPFParagraph> paragraphs = cells.get(ci).getParagraphs();
                        List<List<String>> cellTexts = textoOriginal.get(ci);

                        for (int pi = 0; pi < paragraphs.size() && pi < cellTexts.size(); pi++) {
                            List<XWPFRun> runs = paragraphs.get(pi).getRuns();
                            List<String> runTexts = cellTexts.get(pi);

                            for (int ri = 0; ri < runs.size() && ri < runTexts.size(); ri++) {
                                runs.get(ri).setText(runTexts.get(ri), 0);
                            }
                        }
                    }
                }

                Map<String, String> m = new HashMap<>();
                putAll(m, "ASIGNATURA_NOMBRE", n(ra.getAsignaturaNombre()));
                putAll(m, "RA_CODIGO", n(ra.getRaCodigo()));
                putAll(m, "RA_NUMERO", n(ra.getRaNumero()));

                for (XWPFTableCell cell : currentRow.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, m);
                    }
                }
            }

            return;
        }
    }

    private void copyRowStructure(XWPFTableRow source, XWPFTableRow target) {
        if (source.getCtRow().getTrPr() != null) {
            target.getCtRow().setTrPr(source.getCtRow().getTrPr());
        }

        while (!target.getTableCells().isEmpty()) {
            target.removeCell(0);
        }

        for (XWPFTableCell sourceCell : source.getTableCells()) {
            XWPFTableCell newCell = target.addNewTableCell();

            if (sourceCell.getCTTc().getTcPr() != null) {
                newCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());
            }

            while (!newCell.getParagraphs().isEmpty()) {
                newCell.removeParagraph(0);
            }

            for (XWPFParagraph sourceParagraph : sourceCell.getParagraphs()) {
                XWPFParagraph newParagraph = newCell.addParagraph();

                if (sourceParagraph.getCTP().getPPr() != null) {
                    newParagraph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                }

                for (XWPFRun sourceRun : sourceParagraph.getRuns()) {
                    XWPFRun newRun = newParagraph.createRun();

                    if (sourceRun.getCTR().getRPr() != null) {
                        newRun.getCTR().setRPr(sourceRun.getCTR().getRPr());
                    }

                    String t = sourceRun.getText(0);
                    newRun.setText(t == null ? "" : t, 0);
                }
            }
        }
    }
}
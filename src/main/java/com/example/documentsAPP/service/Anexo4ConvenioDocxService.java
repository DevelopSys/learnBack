package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.ConvenioAnexo4Data;
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
public class Anexo4ConvenioDocxService {

    private static final String TEMPLATE_PATH = "/templates/4_Anexo.docx";

    public byte[] generarAnexo4(ConvenioAnexo4Data data) {
        InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IllegalStateException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        try (InputStream streamCerrado = is;
             XWPFDocument doc = new XWPFDocument(streamCerrado)) {

            Map<String, String> values = buildPlaceholderMap(data);

            // 1. Párrafos sueltos del documento
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraphPreservingFormat(p, values);
            }

            // 2. Tablas (cabeceras, textos fijos, etc.)
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            replaceInParagraphPreservingFormat(p, values);
                        }
                    }
                }
            }

            // 3. Tabla de alumnos (fila plantilla clonada por alumno)
            fillAlumnosTable(doc, data);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Error generando anexo 4", e);
        }
    }

    private Map<String, String> buildPlaceholderMap(ConvenioAnexo4Data d) {
        Map<String, String> m = new HashMap<>();

        m.put("{{CURSO_ANIO}}", n(d.getCursoAnio()));
        m.put("{{CONVENIO_NUMERO}}", n(d.getConvenioNumero()));
        m.put("{{CONVENIO_FECHA}}", n(d.getConvenioFecha()));
        m.put("{{CENTRO_NOMBRE}}", n(d.getCentroNombre()));
        m.put("{{ENTIDAD_NOMBRE}}", n(d.getEntidadNombre()));

        m.put("{{CENTRO_TUTOR_APELLIDO}}", n(d.getCentroTutorApellido()));
        m.put("{{CENTRO_TUTOR_NOMBRE}}", n(d.getCentroTutorNombre()));
        m.put("{{CENTRO_TUTOR_DNI}}", n(d.getCentroTutorDni()));
        m.put("{{CENTRO_TUTOR_MAIL}}", n(d.getCentroTutorMail()));

        // Cubrimos posibles erratas de la plantilla (una "I" o dos "I" en ENTIDAD)
        m.put("{{ENTIDAD_TUTOR_APELLIDO}}", n(d.getEntidadTutorApellido()));
        m.put("{{ENTIIDAD_TUTOR_APELLIDO}}", n(d.getEntidadTutorApellido()));

        m.put("{{ENTIDAD_TUTOR_NOMBRE}}", n(d.getEntidadTutorNombre()));
        m.put("{{ENTIIDAD_TUTOR_NOMBRE}}", n(d.getEntidadTutorNombre()));

        m.put("{{ENTIDAD_TUTOR_DNI}}", n(d.getEntidadTutorDni()));
        m.put("{{ENTIIDAD_TUTOR_DNI}}", n(d.getEntidadTutorDni()));

        m.put("{{ENTIDAD_TUTOR_MAIL}}", n(d.getEntidadTutorMail()));
        m.put("{{ENTIIDAD_TUTOR_MAIL}}", n(d.getEntidadTutorMail()));

        m.put("{{ENTIDAD_DOMICILIO_TRABAJO}}", n(d.getEntidadDomicilioTrabajo()));
        m.put("{{ENTIIDAD_DOMICILIO_TRABAJO}}", n(d.getEntidadDomicilioTrabajo()));

        m.put("{{CURSO_NOMBRE}}", n(d.getCursoNombre()));
        m.put("{{CURSO_NIVEL}}", n(d.getCursoNivel()));
        m.put("{{CURSO_CODIGO}}", n(d.getCursoCodigo()));

        m.put("{{FECHA_FIRMA}}", n(d.getFechaFirma()));

        return m;
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    /**
     * ALGORITMO AVANZADO: Reemplaza variables respetando la división de Runs
     * (esto preserva el formato original del documento: negritas, tamaños, etc.)
     */
    private void replaceInParagraphPreservingFormat(XWPFParagraph paragraph, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = entry.getKey();
            String replacement = entry.getValue();

            while (true) {
                String text = paragraph.getText();
                if (!text.contains(placeholder)) break;

                // Evitar bucle infinito si el reemplazo contiene el placeholder
                if (replacement.equals(placeholder)) break;

                int placeholderStart = text.indexOf(placeholder);
                int placeholderEnd = placeholderStart + placeholder.length();

                int startRunIndex = -1;
                int endRunIndex = -1;
                int posInStartRun = -1;
                int posInEndRun = -1;

                int currentPos = 0;
                List<XWPFRun> runs = paragraph.getRuns();

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

                if (startRunIndex == endRunIndex) {
                    XWPFRun run = runs.get(startRunIndex);
                    String runText = run.getText(0);
                    run.setText(runText.substring(0, posInStartRun) + replacement + runText.substring(posInEndRun), 0);
                } else {
                    XWPFRun startRun = runs.get(startRunIndex);
                    String startRunText = startRun.getText(0);
                    startRun.setText(startRunText.substring(0, posInStartRun) + replacement, 0);

                    for (int i = startRunIndex + 1; i < endRunIndex; i++) {
                        runs.get(i).setText("", 0);
                    }

                    XWPFRun endRun = runs.get(endRunIndex);
                    String endRunText = endRun.getText(0);
                    endRun.setText(endRunText.substring(posInEndRun), 0);
                }
            }
        }
    }

    private void fillAlumnosTable(XWPFDocument doc, ConvenioAnexo4Data data) {
        if (data.getAlumnos() == null || data.getAlumnos().isEmpty()) return;

        for (XWPFTable table : doc.getTables()) {
            XWPFTableRow plantilla = null;
            int plantillaIndex = -1;

            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText();
                    if (cellText != null && cellText.contains("ALUMNO_APELLIDO")) {
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

            for (int idx = 0; idx < data.getAlumnos().size(); idx++) {
                ConvenioAnexo4Data.AlumnoFila al = data.getAlumnos().get(idx);
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
                m.put("{{ALUMNO_APELLIDO}}", n(al.getAlumnoApellido()));
                m.put("{{ALUMNO_NOMBRE}}", n(al.getAlumnoNombre()));
                m.put("{{ALUMNO_DNI}}", n(al.getDni()));
                m.put("{{ALUMNO_FECHA}}", n(al.getFechaNacimiento()));
                m.put("{{PRACTICA_INICIO}}", n(al.getPracticaInicio()));
                m.put("{{PRACTICA_FIN}}", n(al.getPracticaFin()));
                m.put("{{PRACTICA_DIAS}}", n(al.getPracticaDias()));
                m.put("{{PRACTICA_HORA_INICIO}}", n(al.getPracticaHoraInicio()));
                m.put("{{PRACTICA_HORA_FIN}}", n(al.getPracticaHoraFin()));
                m.put("{{PRACTICA_HORA_FIN}", n(al.getPracticaHoraFin()));
                m.put("{{PRACTICA_HORA_SEMANA}}", n(al.getPracticaHorasSemana()));
                m.put("{{PRACTICA_HORAS_TOTAL}}", n(al.getPracticaHorasTotal()));

                // Número de orden (01, 02...) en la primera celda
                if (!currentRow.getTableCells().isEmpty()) {
                    XWPFTableCell c0 = currentRow.getCell(0);
                    for (XWPFParagraph p : c0.getParagraphs()) {
                        List<XWPFRun> runs = p.getRuns();
                        if (runs != null && !runs.isEmpty()) {
                            runs.get(0).setText(n(al.getNumero()), 0);
                            for (int r = 1; r < runs.size(); r++) {
                                runs.get(r).setText("", 0);
                            }
                        }
                    }
                }

                // Reemplazar placeholders usando la lógica que conserva formato
                for (XWPFTableCell cell : currentRow.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraphPreservingFormat(p, m);
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
package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.Anexo9Data;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Anexo9DocxService {

    private static final String TEMPLATE_PATH = "/templates/9_Anexo.docx";

    public byte[] generarAnexo9(Anexo9Data data) {
        InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IllegalStateException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        try (InputStream streamCerrado = is;
             XWPFDocument doc = new XWPFDocument(streamCerrado)) {

            Map<String, String> values = buildPlaceholderMap(data);

            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraphPreservingFormat(p, values);
            }

            for (XWPFTable table : doc.getTables()) {
                procesarTablaRecursiva(table, values);
            }

            fillResultadosTable(doc, data);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Error generando anexo 9", e);
        }
    }

    private void procesarTablaRecursiva(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraphPreservingFormat(p, values);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    procesarTablaRecursiva(nestedTable, values);
                }
            }
        }
    }

    private Map<String, String> buildPlaceholderMap(Anexo9Data d) {
        Map<String, String> m = new HashMap<>();

        m.put("{{CURSO_ANIO}}", n(d.getCursoAnio()));
        m.put("{{CONVENIO_NUMERO}}", n(d.getConvenioNumero()));

        m.put("{{ALUMNO_APELLIDO}}", n(d.getAlumnoApellido()));
        m.put("{{ALUMNO_NOMBRE}}", n(d.getAlumnoNombre()));
        m.put("{{ALUMNO_CORREO}}", n(d.getAlumnoCorreo()));

        m.put("{{ENTIDAD_NOMBRE}}", n(d.getEntidadNombre()));
        m.put("{{ENTIDAD_TUTOR_APELLIDO}}", n(d.getEntidadTutorApellido()));
        m.put("{{ENTIDAD_TUTOR_NOMBRE}}", n(d.getEntidadTutorNombre()));
        m.put("{{ENTIDAD_TUTOR_CORREO}}", n(d.getEntidadTutorCorreo()));

        m.put("{{CURSO_NOMBRE}}", n(d.getCursoNombre()));
        m.put("{{CURSO_CODIGO}}", n(d.getCursoCodigo()));
        m.put("{{PRACTICAS_HORAS}}", n(d.getPracticasHoras()));

        m.put("{{C_1}}", n(d.getC1()));
        m.put("{{C_2}}", n(d.getC2()));
        m.put("{{C_3}}", n(d.getC3()));

        m.put("{{DIA}}", n(d.getDia()));
        m.put("{{MES}}", n(d.getMes()));
        m.put("{{ANIO}}", n(d.getAnio()));

        return m;
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    /**
     * Reemplaza placeholders respetando los runs del documento
     * para preservar formato, negritas, símbolos, etc.
     */
    private void replaceInParagraphPreservingFormat(XWPFParagraph paragraph, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = entry.getKey();
            String replacement = entry.getValue();

            while (true) {
                String text = paragraph.getText();
                if (!text.contains(placeholder)) break;
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
                    run.setText(
                            runText.substring(0, posInStartRun)
                                    + replacement
                                    + runText.substring(posInEndRun),
                            0
                    );
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

    private void fillResultadosTable(XWPFDocument doc, Anexo9Data data) {
        if (data.getResultados() == null || data.getResultados().isEmpty()) return;

        for (XWPFTable table : doc.getTables()) {
            XWPFTableRow plantilla = null;
            int plantillaIndex = -1;

            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText();
                    if (cellText != null &&
                            (cellText.contains("{{COD_ASIGNATURA}}")
                                    || cellText.contains("COD_ASIGNATURA")
                                    || cellText.contains("{{RESULTADO_NUMERO}}")
                                    || cellText.contains("RESULTADO_NUMERO"))) {
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
                Anexo9Data.ResultadoFila ra = data.getResultados().get(idx);
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
                m.put("{{COD_ASIGNATURA}}", n(ra.getCodAsignatura()));
                m.put("{{RESULTADO_NUMERO}}", n(ra.getResultadoNumero()));

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
            target.getCtRow().setTrPr((CTTrPr) source.getCtRow().getTrPr().copy());
        }

        while (!target.getTableCells().isEmpty()) {
            target.removeCell(0);
        }

        for (XWPFTableCell sourceCell : source.getTableCells()) {
            XWPFTableCell newCell = target.addNewTableCell();

            if (sourceCell.getCTTc().getTcPr() != null) {
                newCell.getCTTc().setTcPr((CTTcPr) sourceCell.getCTTc().getTcPr().copy());
            }

            while (!newCell.getParagraphs().isEmpty()) {
                newCell.removeParagraph(0);
            }

            for (XWPFParagraph sourceParagraph : sourceCell.getParagraphs()) {
                XWPFParagraph newParagraph = newCell.addParagraph();

                if (sourceParagraph.getCTP().getPPr() != null) {
                    newParagraph.getCTP().setPPr((CTPPr) sourceParagraph.getCTP().getPPr().copy());
                }

                for (XWPFRun sourceRun : sourceParagraph.getRuns()) {
                    XWPFRun newRun = newParagraph.createRun();
                    newRun.getCTR().set(sourceRun.getCTR().copy());
                }
            }
        }
    }
}
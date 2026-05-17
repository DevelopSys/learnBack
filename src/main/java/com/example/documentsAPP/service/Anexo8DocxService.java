package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.Anexo8Data;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Anexo8DocxService {

    private static final String TEMPLATE_PATH = "/templates/8_Anexo.docx";

    public byte[] generarAnexo8(Anexo8Data data) {
        InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IllegalStateException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        byte[] plantillaBytes;
        try {
            plantillaBytes = is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo plantilla anexo 8", e);
        }

        List<Anexo8Data.PeriodoSeguimiento> periodos = data.getPeriodos();

        if (periodos == null || periodos.isEmpty()) {
            periodos = List.of(new Anexo8Data.PeriodoSeguimiento("", ""));
        }

        try {
// Primer periodo sobre el documento base
            XWPFDocument docFinal = generarPaginaParaPeriodo(plantillaBytes, data, periodos.get(0));

// Periodos adicionales: cada uno se genera aparte y se fusiona
            for (int i = 1; i < periodos.size(); i++) {
                XWPFDocument docPagina = generarPaginaParaPeriodo(plantillaBytes, data, periodos.get(i));
                añadirPaginaAlDocumento(docFinal, docPagina);
                docPagina.close();
            }

            return toBytes(docFinal);

        } catch (IOException e) {
            throw new IllegalStateException("Error generando anexo 8", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
// Genera un XWPFDocument completo para un periodo concreto
// ─────────────────────────────────────────────────────────────────────
    private XWPFDocument generarPaginaParaPeriodo(byte[] plantillaBytes,
                                                  Anexo8Data data,
                                                  Anexo8Data.PeriodoSeguimiento periodo)
            throws IOException {

        XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(plantillaBytes));
        Map<String, String> values = buildPlaceholderMap(data, periodo);

        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraphPreservingFormat(p, values);
        }

        for (XWPFTable table : doc.getTables()) {
            procesarTablaRecursiva(table, values);
        }

        fillResultadosTable(doc, data);

        return doc;
    }

    // ─────────────────────────────────────────────────────────────────────
// Fusiona el contenido de docPagina en docFinal con salto de página
// ─────────────────────────────────────────────────────────────────────
    private void añadirPaginaAlDocumento(XWPFDocument docFinal, XWPFDocument docPagina) {
        CTBody bodyFinal = docFinal.getDocument().getBody();
        CTBody bodyPagina = docPagina.getDocument().getBody();

// Salto de página explícito
        CTP ctpSalto = bodyFinal.addNewP();
        CTR rSalto = ctpSalto.addNewR();
        CTBr br = rSalto.addNewBr();
        br.setType(STBrType.PAGE);

// Clonar párrafos de la página adicional al body final
        for (int i = 0; i < bodyPagina.sizeOfPArray(); i++) {
            bodyFinal.addNewP().set(bodyPagina.getPArray(i).copy());
        }

// Clonar tablas de la página adicional al body final
        for (int i = 0; i < bodyPagina.sizeOfTblArray(); i++) {
            bodyFinal.addNewTbl().set(bodyPagina.getTblArray(i).copy());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
// Mapa de placeholders globales + periodo
// ─────────────────────────────────────────────────────────────────────
    private Map<String, String> buildPlaceholderMap(Anexo8Data d,
                                                    Anexo8Data.PeriodoSeguimiento periodo) {
        Map<String, String> m = new HashMap<>();

        m.put("{{CURSO_ANIO}}", n(d.getCursoAnio()));
        m.put("{{CONVENIO_NUMERO}}", n(d.getConvenioNumero()));

        m.put("{{ALUMNO_APELLIDOS}}", n(d.getAlumnoApellidos()));
        m.put("{{ALUMNO_NOMBRE}}", n(d.getAlumnoNombre()));
        m.put("{{ALUMNO_CORREO}}", n(d.getAlumnoCorreo()));

        m.put("{{ENTIDAD_NOMBRE}}", n(d.getEntidadNombre()));
        m.put("{{ENTIDAD_TUTOR_APELLIDOS}}", n(d.getEntidadTutorApellidos()));
        m.put("{{ENTIDAD_TUTOR_NOMBRE}}", n(d.getEntidadTutorNombre()));
        m.put("{{ENTIDAD_TUTOR_CORREO}}", n(d.getEntidadTutorCorreo()));

        m.put("{{FECHA_INICIO}}", n(periodo.getFechaInicio()));
        m.put("{{FECHA_FIN}}", n(periodo.getFechaFin()));

        return m;
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    // ─────────────────────────────────────────────────────────────────────
// Procesado recursivo de tablas (incluye anidadas)
// ─────────────────────────────────────────────────────────────────────
    private void procesarTablaRecursiva(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraphPreservingFormat(p, values);
                }
                for (XWPFTable nested : cell.getTables()) {
                    procesarTablaRecursiva(nested, values);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
// Reemplazo preservando formato (negritas, tamaños, símbolos)
// ─────────────────────────────────────────────────────────────────────
    private void replaceInParagraphPreservingFormat(XWPFParagraph paragraph,
                                                    Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = entry.getKey();
            String replacement = entry.getValue();

            while (true) {
                String text = paragraph.getText();
                if (!text.contains(placeholder)) break;
                if (replacement.equals(placeholder)) break;

                int pStart = text.indexOf(placeholder);
                int pEnd = pStart + placeholder.length();

                int startRunIndex = -1, endRunIndex = -1;
                int posInStart = -1, posInEnd = -1;
                int currentPos = 0;

                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = 0; i < runs.size(); i++) {
                    String rt = runs.get(i).getText(0);
                    if (rt == null) continue;
                    int rStart = currentPos;
                    int rEnd = currentPos + rt.length();

                    if (startRunIndex == -1 && pStart >= rStart && pStart < rEnd) {
                        startRunIndex = i;
                        posInStart = pStart - rStart;
                    }
                    if (endRunIndex == -1 && pEnd > rStart && pEnd <= rEnd) {
                        endRunIndex = i;
                        posInEnd = pEnd - rStart;
                    }
                    currentPos = rEnd;
                }

                if (startRunIndex == -1 || endRunIndex == -1) break;

                if (startRunIndex == endRunIndex) {
                    XWPFRun run = runs.get(startRunIndex);
                    String rt = run.getText(0);
                    run.setText(rt.substring(0, posInStart) + replacement + rt.substring(posInEnd), 0);
                } else {
                    XWPFRun sr = runs.get(startRunIndex);
                    sr.setText(sr.getText(0).substring(0, posInStart) + replacement, 0);
                    for (int i = startRunIndex + 1; i < endRunIndex; i++) {
                        runs.get(i).setText("", 0);
                    }
                    XWPFRun er = runs.get(endRunIndex);
                    er.setText(er.getText(0).substring(posInEnd), 0);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
// Tabla de resultados de aprendizaje (clona filas con formato completo)
// ─────────────────────────────────────────────────────────────────────
    private void fillResultadosTable(XWPFDocument doc, Anexo8Data data) {
        if (data.getResultados() == null || data.getResultados().isEmpty()) return;

        for (XWPFTable table : doc.getTables()) {
            XWPFTableRow plantilla = null;
            int plantillaIndex = -1;

            for (int i = 0; i < table.getRows().size(); i++) {
                for (XWPFTableCell cell : table.getRow(i).getTableCells()) {
                    String ct = cell.getText();
                    if (ct != null && (ct.contains("CODIGO_MODULO") || ct.contains("RA_NUMERO"))) {
                        plantilla = table.getRow(i);
                        plantillaIndex = i;
                        break;
                    }
                }
                if (plantilla != null) break;
            }

            if (plantilla == null) continue;

            for (int idx = 0; idx < data.getResultados().size(); idx++) {
                Anexo8Data.ResultadoFila ra = data.getResultados().get(idx);
                XWPFTableRow currentRow;

                if (idx == 0) {
                    currentRow = plantilla;
                } else {
                    currentRow = table.insertNewTableRow(plantillaIndex + idx);
                    copyRowStructure(plantilla, currentRow);
                }

                Map<String, String> m = new HashMap<>();
                m.put("{{CODIGO_MODULO}}", n(ra.getCodigoModulo()));
                m.put("{{RA_NUMERO}}", n(ra.getRaNumero()));

                for (XWPFTableCell cell : currentRow.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraphPreservingFormat(p, m);
                    }
                }
            }
            return; // Solo procesamos la primera tabla con resultados
        }
    }

    // ─────────────────────────────────────────────────────────────────────
// Clona estructura de fila preservando XML completo
// (preserva cuadros negros, checkboxes, símbolos especiales)
// ─────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────
// Serializa el documento a bytes
// ─────────────────────────────────────────────────────────────────────
    private byte[] toBytes(XWPFDocument doc) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.write(bos);
        return bos.toByteArray();
    }
}
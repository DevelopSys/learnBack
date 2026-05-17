package com.example.documentsAPP.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class Anexo6TemplateTestService {

    private static final String TEMPLATE_PATH = "/templates/6_Anexo.docx";

    public byte[] descargarPlantillaReescrita() {
        InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new IllegalStateException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        try (InputStream streamCerrado = is;
             XWPFDocument doc = new XWPFDocument(streamCerrado);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            doc.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Error al leer y reescribir la plantilla del anexo 6", e);
        }
    }
}
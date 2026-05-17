package com.example.documentsAPP.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.mail.Session;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

@Service
public class GmailService {

    public void enviarEmail(
            String accessToken,
            String destinatario,
            String asunto,
            String cuerpo,
            Map<String, byte[]> adjuntos
    ) throws Exception {

        // Crear cliente Gmail con el token del usuario
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, null));

        Gmail gmail = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("LearnApp")
                .build();

        // Construir el email con adjuntos
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(destinatario));
        email.setSubject(asunto, "UTF-8");

        MimeMultipart multipart = new MimeMultipart();

        // Cuerpo del mensaje
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(cuerpo, "UTF-8");
        multipart.addBodyPart(textPart);

        // Adjuntos
        for (Map.Entry<String, byte[]> entry : adjuntos.entrySet()) {
            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.setFileName(
                    MimeUtility.encodeText(entry.getKey(), "UTF-8", "B"));
            attachPart.setContent(entry.getValue(),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            multipart.addBodyPart(attachPart);
        }

        email.setContent(multipart);

        // Convertir a Base64 para la API de Gmail
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        String encodedEmail = Base64.getUrlEncoder()
                .encodeToString(buffer.toByteArray());

        Message message = new Message();
        message.setRaw(encodedEmail);

        // Enviar como el usuario autenticado ("me" = token del usuario)
        gmail.users().messages().send("me", message).execute();
    }
}
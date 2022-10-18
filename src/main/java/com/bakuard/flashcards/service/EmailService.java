package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    private ConfigData configData;

    public EmailService(ConfigData configData) {
        this.configData = configData;
    }

    public void confirmEmailForRegistration(String jws, String email) {
        try {
            sendEmail(
                    configData.pathToGmailLetterForRegistration(),
                    configData.gmailLetterReturnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void confirmEmailForRestorePass(String jws, String email) {
        try {
            sendEmail(
                    configData.pathToGmailLetterForRestorePass(),
                    configData.gmailLetterReturnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void confirmEmailForDeletion(String jws, String email) {
        try {
            sendEmail(
                    configData.pathToGmailLetterForDeletion(),
                    configData.gmailLetterReturnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    private void sendEmail(String htmlFileName, String endpoint, String email) throws MessagingException {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtps");
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configData.gmailService(), configData.gmailPassword());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configData.gmailService()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Flash cards");

        InputStream in = getClass().getResourceAsStream(htmlFileName);
        String html = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).
                lines().
                reduce(String::concat).
                orElseThrow().
                replaceAll("endpoint", endpoint);
        message.setContent(html, "text/html; charset=utf-8");

        Transport.send(message);
    }

}

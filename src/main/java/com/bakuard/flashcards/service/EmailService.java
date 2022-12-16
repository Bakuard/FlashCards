package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.validation.FailToSendMailException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Сервис для отправки на почту писем подтверждения при регистрации, смене пароля и удалении аккаунта.
 */
public class EmailService {

    private ConfigData configData;

    /**
     * Создает сервис для отправки на почту писем подтверждения.
     * @param configData общие данные конфигурации приложения
     */
    public EmailService(ConfigData configData) {
        this.configData = configData;
    }

    /**
     * Отправляет на указанный адрес письмо с подтверждением регистрации. В письме содержится ссылка
     * с обратным адресом, в которую включен указанный jws токен.
     * @param jws токен подтверждения регистрации
     * @param email адрес почты на которую отправляется письмо
     */
    public void confirmEmailForRegistration(String jws, String email) {
        try {
            sendEmail(
                    configData.confirmationMail().registration(),
                    configData.confirmationMail().returnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new FailToSendMailException(e, "FailToSendMailException.registration");
        }
    }

    /**
     * Отправляет на указанный адрес письмо с подтверждением восстановления пароля. В письме содержится ссылка
     * с обратным адресом, в которую включен указанный jws токен.
     * @param jws токен восстановления пароля
     * @param email адрес почты на которую отправляется письмо
     */
    public void confirmEmailForRestorePass(String jws, String email) {
        try {
            sendEmail(
                    configData.confirmationMail().restorePass(),
                    configData.confirmationMail().returnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new FailToSendMailException(e, "FailToSendMailException.restorePass");
        }
    }

    /**
     * Отправляет на указанный адрес письмо с подтверждением удаления аккаунта. В письме содержится ссылка
     * с обратным адресом, в которую включен указанный jws токен.
     * @param jws токен удаления аккаунта
     * @param email адрес почты на которую отправляется письмо
     */
    public void confirmEmailForDeletion(String jws, String email) {
        try {
            sendEmail(
                    configData.confirmationMail().deletion(),
                    configData.confirmationMail().returnAddress() + '/' + jws,
                    email
            );
        } catch(MessagingException e) {
            throw new FailToSendMailException(e, "FailToSendMailException.accountDeletion");
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
                return new PasswordAuthentication(
                        configData.smtp().gmailService(),
                        configData.smtp().gmailPassword()
                );
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configData.smtp().gmailService()));
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

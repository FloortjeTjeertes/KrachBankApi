// src/main/java/com/krachbank/api/service/EmailService.java
package com.krachbank.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // Your configured sender email
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String text) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // Set the sender email from properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MailException e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // You might want to rethrow, log to a more robust logging system, or handle differently
            throw e;
        }
    }
}
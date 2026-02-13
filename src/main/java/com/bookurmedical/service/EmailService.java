
package com.bookurmedical.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to BookUrMedical!");
        message.setText("Dear " + firstName + ",\n\n" +
                "Welcome to BookUrMedical! We are thrilled to have you on board.\n\n" +
                "You can now login to your account and start booking your medical appointments.\n\n" +
                "Best regards,\n" +
                "The BookUrMedical Team");

        mailSender.send(message);
    }
}

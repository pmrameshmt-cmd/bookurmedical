
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

    @Value("${app.frontend-url}")
    private String frontendBaseUrl;

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to BookUrMedical!");
        message.setText(
                "Dear " + firstName + ",\n\n" +
                        "Welcome to BookUrMedical! We are thrilled to have you on board.\n\n" +
                        "You can now login to your account and start booking your medical appointments.\n\n" +
                        "Visit us at: " + frontendBaseUrl + "\n\n" +
                        "Best regards,\n" +
                        "The BookUrMedical Team");

        mailSender.send(message);
    }

    @Async
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String verifyLink = frontendBaseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify Your Email - BookUrMedical");
        message.setText(
                "Dear " + firstName + ",\n\n" +
                        "Thank you for registering with BookUrMedical!\n\n" +
                        "Please verify your email address by clicking the link below:\n" +
                        verifyLink + "\n\n" +
                        "This link will remain active until your account is verified.\n\n" +
                        "If you did not create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The BookUrMedical Team");

        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request - BookUrMedical");
        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your BookUrMedical account password.\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link is valid for 1 hour. If you did not request a password reset, " +
                        "please ignore this email — your account is safe.\n\n" +
                        "Best regards,\n" +
                        "The BookUrMedical Team");

        mailSender.send(message);
    }
}

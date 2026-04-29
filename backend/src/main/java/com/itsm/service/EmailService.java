package com.itsm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendOtp(String email, String otp) {
        String subject = "Your Registration OTP - TechNova";
        String body = "Welcome to TechNova!\n\n" +
                "Your One-Time Password (OTP) for registration is: " + otp + "\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.";
        sendEmail(email, subject, body);
    }
}

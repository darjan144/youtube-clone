package isa.vezbe1.spring_boot_example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8084}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendActivationEmail(String toEmail, String activationToken) {
        try {
            String activationLink = baseUrl + "/api/auth/activate?token=" + activationToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Jutjubić - Activate Your Account");
            message.setText(
                    "Welcome to Jutjubić!\n\n" +
                            "Please click the link below to activate your account:\n" +
                            activationLink + "\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you did not create an account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Jutjubić Team"
            );

            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send activation email: " + e.getMessage());
            throw new RuntimeException("Failed to send activation email. Please contact support.");
        }
    }

    public void resendActivationEmail(String toEmail, String activationToken) {
        sendActivationEmail(toEmail, activationToken);
    }
}
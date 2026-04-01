package com.futureprograms.authapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.app-name}")
    private String appName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.backend.url}")
    private String backendUrl;

    /**
     * Envía email de verificación de cuenta
     */
    @Async
    public void sendVerificationEmail(String userEmail, String userName, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String normalizedBackendUrl = backendUrl.endsWith("/")
                ? backendUrl.substring(0, backendUrl.length() - 1)
                : backendUrl;
            String verificationBasePath = normalizedBackendUrl.endsWith("/api")
                ? normalizedBackendUrl
                : normalizedBackendUrl + "/api";
            String verificationLink = verificationBasePath + "/auth/verify/" + verificationToken;

            String htmlContent = buildVerificationEmailHTML(userName, verificationLink);

            helper.setFrom(mailFrom);
            helper.setTo(userEmail);
            helper.setSubject("Verifica tu cuenta - " + appName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", userEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    /**
     * Envía email de bienvenida después de verificación
     */
    @Async
    public void sendWelcomeEmail(String userEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = buildWelcomeEmailHTML(userName);

            helper.setFrom(mailFrom);
            helper.setTo(userEmail);
            helper.setSubject("¡Bienvenido a " + appName + "!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", userEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Failed to send welcome email");
        }
    }

    /**
     * Envía email de recuperación de contraseña
     */
    @Async
    public void sendPasswordResetEmail(String userEmail, String userName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmailHTML(userName, resetLink);

            helper.setFrom(mailFrom);
            helper.setTo(userEmail);
            helper.setSubject("Recuperar contraseña - " + appName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", userEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    private String buildVerificationEmailHTML(String name, String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; }" +
                "    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }" +
                "    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                "    .content { padding: 20px; }" +
                "    .button { background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block; margin: 20px 0; }" +
                "    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" +
                "      <h1>Verifica tu Cuenta</h1>" +
                "    </div>" +
                "    <div class='content'>" +
                "      <p>Hola <strong>" + name + "</strong>,</p>" +
                "      <p>Gracias por registrarte en " + appName + ". Para completar tu registro, por favor verifica tu dirección de correo electrónico haciendo clic en el botón de abajo.</p>" +
                "      <a href='" + verificationLink + "' class='button'>Verificar Email</a>" +
                "      <p>Si no hiciste esta solicitud, ignora este correo.</p>" +
                "      <p>El enlace de verificación expirará en 24 horas.</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>© 2024 " + appName + ". Todos los derechos reservados.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String buildWelcomeEmailHTML(String name) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; }" +
                "    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }" +
                "    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                "    .content { padding: 20px; }" +
                "    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" +
                "      <h1>¡Bienvenido!</h1>" +
                "    </div>" +
                "    <div class='content'>" +
                "      <p>Hola <strong>" + name + "</strong>,</p>" +
                "      <p>¡Tu cuenta ha sido verificada exitosamente! Ahora puedes acceder a todas las funcionalidades de " + appName + ".</p>" +
                "      <p>Estamos felices de tenerte en nuestra comunidad.</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>© 2024 " + appName + ". Todos los derechos reservados.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordResetEmailHTML(String name, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; }" +
                "    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }" +
                "    .header { background-color: #ffc107; color: black; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                "    .content { padding: 20px; }" +
                "    .button { background-color: #ffc107; color: black; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block; margin: 20px 0; }" +
                "    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" +
                "      <h1>Recuperar Contraseña</h1>" +
                "    </div>" +
                "    <div class='content'>" +
                "      <p>Hola <strong>" + name + "</strong>,</p>" +
                "      <p>Recibimos una solicitud para recuperar tu contraseña. Haz clic en el botón de abajo para establecer una nueva contraseña.</p>" +
                "      <a href='" + resetLink + "' class='button'>Recuperar Contraseña</a>" +
                "      <p>Si no solicitaste esto, ignora este correo. El enlace expirará en 1 hora.</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>© 2024 " + appName + ". Todos los derechos reservados.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}

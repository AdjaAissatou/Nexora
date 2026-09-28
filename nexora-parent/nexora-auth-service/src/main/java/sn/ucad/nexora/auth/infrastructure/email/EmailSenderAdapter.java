package sn.ucad.nexora.auth.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.port.outbound.EmailSenderPort;

@Component
public class EmailSenderAdapter implements EmailSenderPort {

    private static final Logger LOG = LoggerFactory.getLogger(EmailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSenderAdapter(JavaMailSender mailSender, @Value("${nexora.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendOtp(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Nexora — Votre code de vérification");
            helper.setText(corpsHtml(otp), true);
            mailSender.send(message);
        } catch (Exception e) {
            LOG.error("Échec de l'envoi de l'email OTP à {}", email, e);
            throw new IllegalStateException("Impossible d'envoyer l'email de vérification.", e);
        }
    }

    private String corpsHtml(String otp) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px 24px;">
                    <h2 style="color:#7a3b2e;margin-bottom:8px;">Nexora</h2>
                    <p>Voici votre code de vérification :</p>
                    <p style="font-size:32px;font-weight:bold;letter-spacing:6px;color:#1f2937;margin:24px 0;">%s</p>
                    <p style="color:#6b7280;font-size:14px;">Ce code expire dans 10 minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                </div>
                """
                .formatted(otp);
    }
}

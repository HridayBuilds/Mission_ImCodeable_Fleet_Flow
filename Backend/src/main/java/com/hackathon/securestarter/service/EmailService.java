package com.hackathon.securestarter.service;

import com.hackathon.securestarter.util.Constants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@fleetflow.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ─── Public send methods ──────────────────────────────────

    /**
     * Send verification email to new user (BLOCKING)
     */
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            String subject = Constants.VERIFICATION_EMAIL_SUBJECT;
            String html = buildVerificationHtml(verificationUrl);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    /**
     * Send password reset email (BLOCKING)
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String subject = Constants.PASSWORD_RESET_EMAIL_SUBJECT;
            String html = buildPasswordResetHtml(resetUrl);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    /**
     * Send welcome email after successful verification (BLOCKING)
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            String loginUrl = frontendUrl + "/login";
            String html = buildWelcomeHtml(firstName, loginUrl);
            sendHtmlEmail(toEmail, "Welcome to Fleet Flow! \uD83D\uDE9B", html);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    // ─── MIME HTML sender ─────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(mimeMessage);
    }

    // ─── Shared layout wrapper ────────────────────────────────

    private String wrapInLayout(String innerContent) {
        return "<!DOCTYPE html>"
                + "<html lang=\"en\"><head><meta charset=\"UTF-8\"/>"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"/>"
                + "<title>Fleet Flow</title></head>"
                + "<body style=\"margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f1f5f9;padding:40px 0;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.06);\">"
                // Header
                + "<tr><td style=\"background:linear-gradient(135deg,#2563eb,#7c3aed);padding:32px 40px;text-align:center;\">"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 auto;\"><tr>"
                + "<td style=\"padding-right:10px;vertical-align:middle;\">"
                + "<div style=\"width:36px;height:36px;background-color:rgba(255,255,255,0.2);border-radius:8px;text-align:center;line-height:36px;font-size:20px;\">&#128666;</div>"
                + "</td>"
                + "<td style=\"vertical-align:middle;\">"
                + "<span style=\"font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;\">Fleet Flow</span>"
                + "</td></tr></table></td></tr>"
                // Body
                + "<tr><td style=\"padding:40px 40px 32px 40px;\">"
                + innerContent
                + "</td></tr>"
                // Footer
                + "<tr><td style=\"padding:0 40px 32px 40px;text-align:center;\">"
                + "<hr style=\"border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;\"/>"
                + "<p style=\"margin:0;font-size:12px;color:#94a3b8;line-height:1.6;\">"
                + "&copy; 2026 Fleet Flow &mdash; Fleet Management System<br/>"
                + "This is an automated message. Please do not reply directly."
                + "</p></td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String buildButton(String url, String label, String bgColor) {
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:28px auto;\">"
                + "<tr><td style=\"border-radius:8px;background-color:" + bgColor + ";\">"
                + "<a href=\"" + url + "\" target=\"_blank\" "
                + "style=\"display:inline-block;padding:14px 36px;font-size:15px;font-weight:600;"
                + "color:#ffffff;text-decoration:none;border-radius:8px;\">"
                + label + "</a></td></tr></table>";
    }

    // ─── HTML template builders ───────────────────────────────

    private String buildVerificationHtml(String verificationUrl) {
        String inner = "<h1 style=\"margin:0 0 8px 0;font-size:22px;font-weight:700;color:#1e293b;\">Verify Your Email</h1>"
                + "<p style=\"margin:0 0 20px 0;font-size:15px;color:#64748b;line-height:1.6;\">"
                + "Thank you for signing up for <strong>Fleet Flow</strong>! "
                + "Please verify your email address to activate your account and start managing your fleet.</p>"
                + buildButton(verificationUrl, "Verify Email Address", "#2563eb")
                + "<p style=\"margin:0 0 8px 0;font-size:13px;color:#94a3b8;line-height:1.5;\">"
                + "This link will expire in <strong>24 hours</strong>.</p>"
                + "<p style=\"margin:0;font-size:13px;color:#94a3b8;line-height:1.5;\">"
                + "If the button doesn't work, copy and paste this URL into your browser:</p>"
                + "<p style=\"margin:6px 0 0 0;font-size:12px;word-break:break-all;\">"
                + "<a href=\"" + verificationUrl + "\" style=\"color:#2563eb;\">" + verificationUrl + "</a></p>";
        return wrapInLayout(inner);
    }

    private String buildPasswordResetHtml(String resetUrl) {
        String inner = "<h1 style=\"margin:0 0 8px 0;font-size:22px;font-weight:700;color:#1e293b;\">Reset Your Password</h1>"
                + "<p style=\"margin:0 0 20px 0;font-size:15px;color:#64748b;line-height:1.6;\">"
                + "We received a request to reset your password for your <strong>Fleet Flow</strong> account. "
                + "Click the button below to choose a new password.</p>"
                + buildButton(resetUrl, "Reset Password", "#7c3aed")
                + "<p style=\"margin:0 0 8px 0;font-size:13px;color:#94a3b8;line-height:1.5;\">"
                + "This link will expire in <strong>1 hour</strong>.</p>"
                + "<p style=\"margin:0 0 8px 0;font-size:13px;color:#94a3b8;line-height:1.5;\">"
                + "If you didn&#39;t request a password reset, you can safely ignore this email "
                + "and your password will remain unchanged.</p>"
                + "<p style=\"margin:0;font-size:12px;word-break:break-all;\">"
                + "<a href=\"" + resetUrl + "\" style=\"color:#2563eb;\">" + resetUrl + "</a></p>";
        return wrapInLayout(inner);
    }

    private String buildWelcomeHtml(String firstName, String loginUrl) {
        String name = (firstName != null && !firstName.isBlank()) ? firstName : "there";
        String inner = "<h1 style=\"margin:0 0 8px 0;font-size:22px;font-weight:700;color:#1e293b;\">Welcome aboard, "
                + name + "! &#127881;</h1>"
                + "<p style=\"margin:0 0 20px 0;font-size:15px;color:#64748b;line-height:1.6;\">"
                + "Your email has been verified and your <strong>Fleet Flow</strong> account is now active. "
                + "You&#39;re all set to manage your fleet efficiently.</p>"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:24px;\">"
                + "<tr><td style=\"padding:16px 20px;background-color:#f0fdf4;border-radius:10px;border-left:4px solid #16a34a;\">"
                + "<p style=\"margin:0;font-size:14px;font-weight:600;color:#166534;\">"
                + "&#10003; &nbsp;Account verified &amp; ready to go</p></td></tr></table>"
                + buildButton(loginUrl, "Go to Dashboard", "#16a34a")
                + "<p style=\"margin:0;font-size:13px;color:#94a3b8;line-height:1.5;\">"
                + "If you have any questions, feel free to reach out to the Fleet Flow support team.</p>";
        return wrapInLayout(inner);
    }
}

package com.subscription.platform.notification.service;

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

    @Value("${app.mail.from:no-reply@subscriptionplatform.local}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String recipientName, String otp) {
        log.info("Preparing OTP email for recipient: {}", toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your SubPulse Verification Code: " + otp);

            String htmlBody = buildOtpHtmlTemplate(recipientName, otp);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("OTP verification email successfully dispatched to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            // In local/sandbox environments, we log the OTP prominently so development is unblocked
            log.warn("=== [FALLBACK OTP LOG] Recipient: {}, OTP: {} ===", toEmail, otp);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
            log.warn("=== [FALLBACK OTP LOG] Recipient: {}, OTP: {} ===", toEmail, otp);
        }
    }

    private String buildOtpHtmlTemplate(String recipientName, String otp) {
        String greeting = (recipientName != null && !recipientName.isBlank()) ? recipientName : "there";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verification Code</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                    .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
                    .header { background: #4f46e5; padding: 24px; text-align: center; }
                    .header h1 { color: #ffffff; font-size: 20px; margin: 0; font-weight: 700; letter-spacing: -0.5px; }
                    .content { padding: 32px 24px; text-align: center; }
                    .greeting { font-size: 16px; margin-bottom: 12px; text-align: left; }
                    .instruction { font-size: 14px; color: #64748b; line-height: 1.5; margin-bottom: 24px; text-align: left; }
                    .otp-box { background: #f1f5f9; border: 2px dashed #cbd5e1; border-radius: 8px; padding: 18px 24px; font-size: 32px; font-weight: 800; letter-spacing: 10px; color: #4f46e5; display: inline-block; margin: 0 auto 24px auto; }
                    .security-note { font-size: 12px; color: #94a3b8; line-height: 1.4; border-top: 1px solid #f1f5f9; padding-top: 20px; text-align: left; }
                    .footer { background: #f8fafc; padding: 16px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>SubPulse Platform</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">Hi %s,</div>
                        <div class="instruction">
                            Use the verification code below to complete your login. This code is confidential and will expire in <strong>5 minutes</strong>.
                        </div>
                        <div class="otp-box">%s</div>
                        <div class="security-note">
                            <strong>Security tip:</strong> If you did not initiate this login request, no action is needed. Your account remains completely secure. Never share this code with anyone.
                        </div>
                    </div>
                    <div class="footer">
                        &copy; SubPulse Platform • Secure Multi-Tenant Architecture
                    </div>
                </div>
            </body>
            </html>
            """.formatted(greeting, otp);
    }
}


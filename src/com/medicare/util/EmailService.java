package com.medicare.util;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.medicare.model.Appointment;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String SENDER_EMAIL = "medicaremanagement2026@gmail.com";
    private static final String SENDER_PASSWORD = System.getenv("SMTP_PASSWORD");

    public static void sendBookingConfirmation(final String recipientEmail, final String recipientName, final Appointment appt) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.err.println("❌ [EmailService] Recipient email is null or empty!");
            return;
        }

        if (SENDER_PASSWORD == null || SENDER_PASSWORD.trim().isEmpty()) {
            System.err.println("[EmailService] SMTP_PASSWORD environment variable is not configured; confirmation email was not sent.");
            return;
        }

        final String cleanRecipient = recipientEmail.trim();

        // Run in background thread so user dashboard loads without delay
        new Thread(() -> {
            try {
                System.out.println("==================================================");
                System.out.println("🚀 [EmailService] SENDING LIVE CONFIRMATION EMAIL");
                System.out.println("   From: " + SENDER_EMAIL);
                System.out.println("   To:   " + cleanRecipient);
                System.out.println("   Ref:  " + appt.getBookingReference());
                System.out.println("==================================================");

                Properties props = new Properties();
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", SMTP_PORT);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, "MediCare Diagnostic Centers"));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(cleanRecipient));
                message.setSubject("Appointment Confirmed: " + appt.getBookingReference() + " - MediCare Bangalore", "UTF-8");

                String htmlContent = buildHtmlTemplate(recipientName, appt);
                message.setContent(htmlContent, "text/html; charset=UTF-8");

                Transport.send(message);
                System.out.println("🎉 [EmailService] SUCCESS: Live Confirmation Email Delivered to " + cleanRecipient);

            } catch (Exception e) {
                System.err.println("❌ [EmailService] ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private static String buildHtmlTemplate(String name, Appointment a) {
        String serviceBadge = "HOME_COLLECTION".equalsIgnoreCase(a.getServiceType()) 
            ? "<span style='color: #0d9488; font-weight: bold;'>🏠 Home Sample Collection</span>" 
            : "<span style='color: #059669; font-weight: bold;'>🏥 Diagnostic Center Visit</span>";

        String paymentInfo = "PAY_AT_CENTER".equalsIgnoreCase(a.getPaymentMethod())
            ? "<span style='color: #475569;'>Pay at Center (Pending upon arrival)</span>"
            : "<span style='color: #16a34a; font-weight: bold;'>" + a.getPaymentMethod() + " (Paid Online)</span>";

        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'><style>" +
               "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f1f5f9; color: #1e293b; margin: 0; padding: 25px; }" +
               ".container { max-width: 620px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #cbd5e1; overflow: hidden; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.07); }" +
               ".header { background: linear-gradient(135deg, #059669, #0d9488); padding: 28px; text-align: center; color: #ffffff; }" +
               ".body { padding: 32px; }" +
               ".badge { background-color: #ecfdf5; color: #065f46; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 50px; display: inline-block; margin-bottom: 15px; }" +
               ".table { width: 100%; border-collapse: collapse; margin: 20px 0; background: #f8fafc; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }" +
               ".table td { padding: 12px 16px; border-bottom: 1px solid #e2e8f0; font-size: 14px; }" +
               ".table td:first-child { font-weight: 700; color: #475569; width: 38%; }" +
               ".guidelines { background: #ecfdf5; border-left: 4px solid #059669; padding: 14px 18px; margin: 24px 0; border-radius: 6px; }" +
               ".footer { background: #0f172a; color: #94a3b8; text-align: center; padding: 22px; font-size: 12px; line-height: 1.6; }" +
               "</style></head><body>" +
               "<div class='container'>" +
               "  <div class='header'>" +
               "    <h1 style='margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;'>MediCare Diagnostic Center</h1>" +
               "    <p style='margin: 6px 0 0; font-size: 14px; opacity: 0.95;'>Bangalore Accredited Clinical Laboratory Network</p>" +
               "  </div>" +
               "  <div class='body'>" +
               "    <span class='badge'>✓ APPOINTMENT CONFIRMED</span>" +
               "    <h2 style='color: #0f172a; margin-top: 0; font-size: 20px;'>Your Booking is Confirmed!</h2>" +
               "    <p style='line-height: 1.6;'>Dear <strong>" + (name != null ? name : "Patient") + "</strong>,</p>" +
               "    <p style='line-height: 1.6; color: #475569;'>Your diagnostic test appointment with MediCare Bangalore has been confirmed. Details are below:</p>" +
               "    <table class='table'>" +
               "      <tr><td>Booking Reference</td><td><strong style='font-family: monospace; font-size: 16px; color: #059669;'>" + a.getBookingReference() + "</strong></td></tr>" +
               "      <tr><td>Bangalore Center</td><td><strong>" + a.getBranchName() + "</strong></td></tr>" +
               "      <tr><td>Scheduled Date</td><td><strong>" + a.getAppointmentDate() + "</strong></td></tr>" +
               "      <tr><td>Time Slot</td><td>" + a.getTimeSlot() + "</td></tr>" +
               "      <tr><td>Service Mode</td><td>" + serviceBadge + "</td></tr>" +
               "      <tr><td>Tests / Packages</td><td><strong>" + a.getItemBooked() + "</strong></td></tr>" +
               "      <tr><td>Total Amount</td><td><strong style='font-size: 15px;'>₹" + String.format("%.2f", a.getTotalAmount()) + "</strong></td></tr>" +
               "      <tr><td>Payment Status</td><td>" + paymentInfo + "</td></tr>" +
               "    </table>" +
               "    <div class='guidelines'>" +
               "      <strong style='color: #065f46; font-size: 14px;'>Clinical Preparation Guidelines:</strong>" +
               "      <ul style='margin: 8px 0 0; padding-left: 20px; font-size: 13px; color: #065f46; line-height: 1.6;'>" +
               "        <li>For Lipid Profile, Glucose & Wellness Panels, 10-12 hours of overnight fasting is required (water is permitted).</li>" +
               "        <li>Digital verified laboratory reports will be available for download on your Patient Dashboard within 4-8 hours.</li>" +
               "        <li>For imaging tests (X-Ray/Scan), physical films must be collected directly from the center.</li>" +
               "      </ul>" +
               "    </div>" +
               "    <p style='font-size: 13px; color: #64748b;'>You can view, reschedule, or cancel this booking anytime from your <a href='http://localhost:8080/DiagnosticCenterSystem/login' style='color: #059669; font-weight: 700;'>Patient Dashboard</a>.</p>" +
               "  </div>" +
               "  <div class='footer'>" +
               "    <p style='margin: 0;'><strong>MediCare Diagnostic Centers &bull; Bangalore</strong></p>" +
               "    <p style='margin: 4px 0 0;'>Shivajinagar &bull; Whitefield &bull; MG Road &bull; Kamanahalli</p>" +
               "    <p style='margin: 6px 0 0; font-size: 11px; opacity: 0.8;'>Bangalore Central Helpline: 080-41123300 | &copy; 2026 MediCare. All rights reserved.</p>" +
               "  </div>" +
               "</div></body></html>";
    }
}
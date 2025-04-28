package gr.hua.dit.preventiveHealth.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import gr.hua.dit.preventiveHealth.config.email.GmailAuth;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Service
public class GmailService {

    @Autowired
    private GmailAuth gmailAuth;

    private Gmail getService() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var credential = gmailAuth.getCredentials();
        return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Gmail Sender")
                .build();
    }

    public void sendEmail(String to, String subject, String bodyText){
        try{
            MimeMessage email = createEmail(to, "me", subject, bodyText);
            Message message = createMessageWithEmail(email);
            getService().users().messages().send("me", message).execute();
        }catch (Exception e){
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        String companyName = "PreventiveHealth";

        String formattedText =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; color: #333;'>" +
                        "<div style='text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eaeaea;'>" +
                        "<h1 style='color: #0066cc; margin-top: 10px;'>" + companyName + "</h1>" +
                        "</div>" +

                        // Body content
                        "<div style='padding: 20px 0; line-height: 1.6;'>" +
                        bodyText.replace("\n", "<br>") +
                        "</div>" +

                        // Footer
                        "<div style='padding-top: 20px; border-top: 1px solid #eaeaea; font-size: 0.9em; color: #777;'>" +
                        "<p>This is an automated email from " + companyName + ".<br>Please do not reply to this email.</p>" +
                        "<div style='margin-top: 20px; text-align: center;'>" +
                        "</div>" +
                        "<p style='text-align: center; margin-top: 20px;'>&copy; " +
                        java.time.Year.now().getValue() + " " + companyName + ". All rights reserved.</p>" +
                        "</div>" +
                        "</div>";

        email.setContent(formattedText, "text/html; charset=utf-8");
        return email;
    }

    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
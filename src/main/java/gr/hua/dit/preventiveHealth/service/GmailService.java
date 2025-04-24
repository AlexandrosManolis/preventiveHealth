package gr.hua.dit.preventiveHealth.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import gr.hua.dit.preventiveHealth.config.email.GmailAuth;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Autowired;
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
        return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credential))
                .setApplicationName("Gmail Sender")
                .build();
    }

    public void sendEmail(String to, String subject, String bodyText) throws Exception {
        MimeMessage email = createEmail(to, "me", subject, bodyText);
        Message message = createMessageWithEmail(email);
        getService().users().messages().send("me", message).execute();
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
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
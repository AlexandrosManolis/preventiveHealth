package gr.hua.dit.preventiveHealth.config.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;

@Service
public class GmailAuth {

    private static Credential credential;
    private static final long EXPIRY_THRESHOLD_MS = 60_000;
    private static final String TOKEN_DIR = "tokens";
    private static final String CREDENTIALS_PATH = "/credentials.json";
    private static final String CREDENTIAL_HASH_PATH = TOKEN_DIR + "/credentials.hash";

    public synchronized Credential getCredentials() throws Exception {
        if (credential == null) {
            initialize();
        }

        // Check if token is expiring soon
        if (credential.getExpiresInSeconds() != null &&
                credential.getExpiresInSeconds() <= EXPIRY_THRESHOLD_MS / 1000) {
            System.out.println("🔄 Refreshing token...");
            credential.refreshToken();
        } else {
            System.out.println("✅ Token is valid. Using cached credentials.");
        }

        return credential;
    }

    private void initialize() throws Exception {
        System.out.println("🔐 Checking credentials...");

        InputStream in = getClass().getResourceAsStream(CREDENTIALS_PATH);
        if (in == null) {
            throw new FileNotFoundException("Missing credentials.json in resources folder.");
        }

        // Check hash
        byte[] credentialsBytes = in.readAllBytes();
        String currentHash = sha256(credentialsBytes);
        File hashFile = new File(CREDENTIAL_HASH_PATH);

        if (!hashFile.exists() || !Files.readString(hashFile.toPath()).equals(currentHash)) {
            System.out.println("🆕 credentials.json has changed. Resetting token.");
            deleteTokensFolder();
            Files.createDirectories(Paths.get(TOKEN_DIR));
            Files.writeString(hashFile.toPath(), currentHash);
        }

        // Reload input stream for actual use
        InputStream secretsStream = new ByteArrayInputStream(credentialsBytes);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new InputStreamReader(secretsStream)
        );

        var flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                Collections.singletonList("https://www.googleapis.com/auth/gmail.send")
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKEN_DIR)))
                .setAccessType("offline")
                .build();

        credential = flow.loadCredential("user");

        if (credential == null || credential.getAccessToken() == null ||
                (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60)) {
            System.out.println("🔐 No valid token. Starting authentication...");
            var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            System.out.println("✅ Token saved to " + TOKEN_DIR);
        } else {
            System.out.println("✅ Valid Gmail token found.");
        }
    }

    private boolean isExpiringSoon(Credential credential) {
        if (credential.getExpiresInSeconds() == null) return true;
        return credential.getExpiresInSeconds() <= EXPIRY_THRESHOLD_MS / 1000;
    }

    private void deleteTokensFolder() {
        try {
            Path tokensPath = Paths.get(TOKEN_DIR);
            if (Files.exists(tokensPath)) {
                Files.walk(tokensPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                System.out.println("🗑️ Deleted old token folder.");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Failed to delete old tokens: " + e.getMessage());
        }
    }

    private String sha256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input);
        return Base64.getEncoder().encodeToString(hash);
    }

    public Gmail getGmailService() throws Exception {
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                getCredentials())
                .setApplicationName("Gmail Sender")
                .build();
    }
}
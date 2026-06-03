package com.raposo.habittracker.infrastructure.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.raposo.habittracker.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleAuth {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS_READONLY);

    private final Config config;

    public GoogleAuth(Config config) {
        this.config = config;
    }

    public Sheets createSheetsService() {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Sheets.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    getCredentials(httpTransport))
                    .setApplicationName(config.APPLICATION_NAME)
                    .build();

        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to create Google Sheets service", exception);
        }
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        if (!Files.exists(config.CREDENTIALS_PATH)) {
            throw new IOException(config.CREDENTIALS_PATH + " not found.");
        }

        try (InputStream inputStream = Files.newInputStream(config.CREDENTIALS_PATH)) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(inputStream));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(config.TOKENS_DIRECTORY_PATH.toFile()))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            return new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");
        }
    }
}
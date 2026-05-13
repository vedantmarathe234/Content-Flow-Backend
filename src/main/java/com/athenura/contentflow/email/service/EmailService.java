package com.athenura.contentflow.email.service;

import com.athenura.contentflow.email.dto.EmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private static final MediaType JSON =
            MediaType.parse("application/json");

    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public EmailService(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper
    ) {

        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    public String sendEmail(
            EmailRequest emailRequest
    ) {

        validateEmailRequest(emailRequest);

        try {

            String requestBodyJson =
                    buildRequestBody(emailRequest);

            RequestBody requestBody =
                    RequestBody.create(
                            requestBodyJson,
                            JSON
                    );

            Request request =
                    buildRequest(requestBody);

            return executeRequest(request);

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to send email",
                    exception
            );
        }
    }

    private void validateEmailRequest(
            EmailRequest emailRequest
    ) {

        if (emailRequest.getTo() == null
                || emailRequest.getTo().isBlank()) {

            throw new IllegalArgumentException(
                    "Receiver email is required"
            );
        }

        if (emailRequest.getSubject() == null
                || emailRequest.getSubject().isBlank()) {

            throw new IllegalArgumentException(
                    "Subject is required"
            );
        }

        if (emailRequest.getBody() == null
                || emailRequest.getBody().isBlank()) {

            throw new IllegalArgumentException(
                    "Email body is required"
            );
        }
    }

    private String buildRequestBody(
            EmailRequest emailRequest
    ) throws IOException {

        Map<String, Object> payload = Map.of(

                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),

                "to", List.of(
                        Map.of(
                                "email",
                                emailRequest.getTo()
                        )
                ),

                "subject",
                emailRequest.getSubject(),

                "htmlContent",
                emailRequest.getBody()
        );

        return objectMapper.writeValueAsString(payload);
    }

    private Request buildRequest(
            RequestBody requestBody
    ) {

        return new Request.Builder()
                .url(BREVO_API_URL)
                .post(requestBody)
                .addHeader(
                        "accept",
                        "application/json"
                )
                .addHeader(
                        "api-key",
                        brevoApiKey
                )
                .addHeader(
                        "content-type",
                        "application/json"
                )
                .build();
    }

    private String executeRequest(
            Request request
    ) {

        try (Response response =
                     okHttpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {

                throw new RuntimeException(
                        "Brevo API request failed"
                );
            }

            return "Email sent successfully";

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Error while sending email",
                    exception
            );
        }
    }
}
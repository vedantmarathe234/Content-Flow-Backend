package com.athenura.contentflow.email.service;

import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
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
import java.util.Optional;

@Service
public class EmailService {

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private static final MediaType JSON =
            MediaType.parse("application/json");

    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;


    public EmailService(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper,
            UserRepository userRepository
    ) {
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public String sendEmail(
            EmailRequest emailRequest
    ) {

        validateEmailRequest(emailRequest);

        try {

            String targetEmail = emailRequest.getTo();
            Optional<User> userOptional = userRepository.findByEmail(targetEmail);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.getRole() == Role.INTERN && user.getTeam() != null && user.getTeam().getTeamLeader() != null) {

                    targetEmail = user.getTeam().getTeamLeader().getEmail();
                    emailRequest.setTo(targetEmail);
                }
            }


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
                        "Brevo API request failed: "
                                + response.body().string()
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
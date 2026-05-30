package com.codespace.tutorias.Helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class EmailHelper {

    private static final String SENDGRID_ENDPOINT = "https://api.sendgrid.com/v3/mail/send";

    private final RestClient restClient = RestClient.create();

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from}")
    private String from;

    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {

        Map<String, Object> body = Map.of(
                "personalizations", List.of(Map.of(
                        "to", List.of(Map.of("email", destinatario))
                )),
                "from", Map.of("email", from),
                "subject", asunto,
                "content", List.of(Map.of(
                        "type", "text/html",
                        "value", cuerpoHtml
                ))
        );

        restClient.post()
                .uri(SENDGRID_ENDPOINT)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}

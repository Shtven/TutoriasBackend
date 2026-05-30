package com.codespace.tutorias.Helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class EmailHelper {

    private static final String RESEND_ENDPOINT = "https://api.resend.com/emails";

    private final RestClient restClient = RestClient.create();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {

        Map<String, Object> body = Map.of(
                "from", from,
                "to", List.of(destinatario),
                "subject", asunto,
                "html", cuerpoHtml
        );

        restClient.post()
                .uri(RESEND_ENDPOINT)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}

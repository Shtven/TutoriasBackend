package com.codespace.tutorias;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * /v3/api-docs describe la respuesta de cada endpoint (ApiResponse<X>), para
 * que el front pueda generar sus tipos y detectar cambios que lo rompan.
 */
class OpenApiIntegracionTest extends PruebaIntegracion {

    private String apiDocs() throws Exception {
        return mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
    }

    @Test
    @SuppressWarnings("unchecked")
    void todaRespuesta200TieneEsquema() throws Exception {
        String docs = apiDocs();
        Map<String, Map<String, Object>> paths = JsonPath.read(docs, "$.paths");

        List<String> sinTipo = new ArrayList<>();
        paths.forEach((ruta, operaciones) -> operaciones.forEach((metodo, op) -> {
            Map<String, Object> ok = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) op)
                    .get("responses")).get("200");
            Map<String, Object> contenido = ok == null ? null : (Map<String, Object>) ok.get("content");
            boolean tipado = contenido != null && contenido.values().stream()
                    .map(c -> (Map<String, Object>) ((Map<String, Object>) c).get("schema"))
                    .allMatch(schema -> schema != null && String.valueOf(schema.get("$ref")).startsWith("#/components/schemas/ApiResponse"));
            if (!tipado) sinTipo.add(metodo.toUpperCase() + " " + ruta);
        }));

        assertThat(paths).isNotEmpty();
        assertThat(sinTipo).as("endpoints sin ResponseEntity<ApiResponse<X>>").isEmpty();
    }

    @Test
    void elSobreDescribeDataConSuTipo() throws Exception {
        String docs = apiDocs();

        String ref = JsonPath.read(docs, "$.paths['/auth/signin'].post.responses['200'].content['*/*'].schema['$ref']");
        String sobre = ref.substring(ref.lastIndexOf('/') + 1);
        assertThat((String) JsonPath.read(docs, "$.components.schemas['" + sobre + "'].properties.data['$ref']"))
                .endsWith("/LoginResponsive");
        assertThat((Map<String, Object>) JsonPath.read(docs, "$.components.schemas.LoginResponsive.properties"))
                .containsOnlyKeys("token", "usuario");

        // materia es objeto en todas las respuestas (punto 4 de los pendientes del front).
        for (String esquema : new String[]{"SesionResponsive", "HistorialTutoriaResponsive", "ProximaSesionResponsive",
                "ChoqueConResponsive", "SemanaItemResponsive", "TutoriaResumenResponsive"}) {
            assertThat((String) JsonPath.read(docs, "$.components.schemas." + esquema + ".properties.materia['$ref']"))
                    .as(esquema).endsWith("/MateriaResponsive");
        }
    }
}

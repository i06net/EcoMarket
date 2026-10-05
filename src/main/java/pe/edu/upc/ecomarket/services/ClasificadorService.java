package pe.edu.upc.ecomarket.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;

import java.text.Normalizer;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClasificadorService {

    public static final String MOTOR_GEMINI = "gemini";
    public static final String MOTOR_KEYWORDS = "keywords";

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public ClasificadorService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.modelo}") String modelo,
            @Value("${gemini.url}") String url,
            @Value("${gemini.tiempo-espera-segundos}") int tiempoEspera) {

        SimpleClientHttpRequestFactory tiempos =
                new SimpleClientHttpRequestFactory();

        tiempos.setConnectTimeout(Duration.ofSeconds(tiempoEspera));
        tiempos.setReadTimeout(Duration.ofSeconds(tiempoEspera));

        this.apiKey = apiKey;
        this.modelo = modelo;

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(tiempos)
                .build();
    }

    public Resultado clasificar(
            String texto,
            List<EcoEtiqueta> etiquetas) {

        if (apiKey != null && !apiKey.isBlank() && !etiquetas.isEmpty()) {

            Set<EcoEtiqueta> resultado =
                    consultarGemini(texto, etiquetas);

            if (resultado != null) {
                return new Resultado(resultado, MOTOR_GEMINI);
            }
        }

        return new Resultado(
                porPalabrasClave(texto, etiquetas),
                MOTOR_KEYWORDS
        );
    }

    public record Resultado(
            Set<EcoEtiqueta> ecoEtiquetas,
            String motor) {
    }

    private Set<EcoEtiqueta> consultarGemini(
            String texto,
            List<EcoEtiqueta> etiquetas) {

        try {
            Map<String, Object> cuerpo = Map.of(
                    "contents",
                    List.of(
                            Map.of(
                                    "parts",
                                    List.of(
                                            Map.of(
                                                    "text",
                                                    armarPrompt(texto, etiquetas)
                                            )
                                    )
                            )
                    )
            );

            RespuestaGemini respuesta = restClient.post()
                    .uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(RespuestaGemini.class);

            if (respuesta == null) {
                return null;
            }

            String textoRespuesta = respuesta.primerTexto();

            if (textoRespuesta == null) {
                return null;
            }

            return idsElegidos(textoRespuesta, etiquetas);

        } catch (RestClientException e) {
            return null;
        }
    }

    private static String armarPrompt(
            String texto,
            List<EcoEtiqueta> etiquetas) {

        String lista = etiquetas.stream()
                .map(e ->
                        "- id " + e.getId()
                                + ": " + e.getNombre()
                                + ". " + valor(e.getDescripcion())
                                + " Criterio: " + valor(e.getCriterio()))
                .collect(Collectors.joining("\n"));

        return """
                Analiza el nombre y la información del producto.

                Estas son las Eco-Etiquetas disponibles:
                %s

                Producto:
                %s

                Selecciona las Eco-Etiquetas que correspondan al producto.

                Responde solo con los números de las etiquetas separados por comas.
                Si ninguna corresponde, responde NINGUNA.
                """.formatted(lista, texto);
    }

    private static Set<EcoEtiqueta> idsElegidos(
            String respuesta,
            List<EcoEtiqueta> etiquetas) {

        Set<EcoEtiqueta> elegidas = new HashSet<>();

        for (String numero : respuesta.split("\\D+")) {

            if (numero.isEmpty() || numero.length() > 18) {
            }

            Long id = Long.valueOf(numero);

            for (EcoEtiqueta etiqueta : etiquetas) {

                if (etiqueta.getId().equals(id)) {
                    elegidas.add(etiqueta);
                }
            }
        }

        return elegidas;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaGemini(List<Candidato> candidates) {

        String primerTexto() {

            if (candidates == null || candidates.isEmpty()) {
                return null;
            }

            Candidato candidato = candidates.getFirst();

            if (candidato.content() == null ||
                    candidato.content().parts() == null) {
                return null;
            }

            return candidato.content().parts().stream()
                    .map(Parte::text)
                    .filter(t -> t != null)
                    .collect(Collectors.joining(" "));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidato(Contenido content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Contenido(List<Parte> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Parte(String text) {
    }

    public Set<EcoEtiqueta> porPalabrasClave(
            String texto,
            List<EcoEtiqueta> etiquetas) {

        String normalizado = normalizar(texto);

        Set<EcoEtiqueta> elegidas = new HashSet<>();

        for (EcoEtiqueta etiqueta : etiquetas) {

            if (coincide(normalizado, etiqueta)) {
                elegidas.add(etiqueta);
            }
        }

        return elegidas;
    }

    private static boolean coincide(
            String texto,
            EcoEtiqueta etiqueta) {

        if (texto.contains(normalizar(etiqueta.getNombre()))) {
            return true;
        }

        if (etiqueta.getPalabrasClave() == null) {
            return false;
        }

        for (String palabra : etiqueta.getPalabrasClave().split(",")) {

            palabra = palabra.trim();

            if (!palabra.isEmpty() &&
                    texto.contains(normalizar(palabra))) {
                return true;
            }
        }

        return false;
    }

    static String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}
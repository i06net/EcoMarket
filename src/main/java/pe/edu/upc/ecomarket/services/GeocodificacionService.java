package pe.edu.upc.ecomarket.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.ecomarket.dto.CoordenadasDTO;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class GeocodificacionService {

    private final RestClient restClient;
    private final boolean habilitada;

    public GeocodificacionService(
            @Value("${geocodificacion.habilitada}") boolean habilitada,
            @Value("${geocodificacion.url}") String url,
            @Value("${geocodificacion.agente}") String agente) {

        SimpleClientHttpRequestFactory tiempos =
                new SimpleClientHttpRequestFactory();

        tiempos.setConnectTimeout(Duration.ofSeconds(5));
        tiempos.setReadTimeout(Duration.ofSeconds(5));

        this.habilitada = habilitada;

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(tiempos)
                .defaultHeader("User-Agent", agente)
                .build();
    }

    public CoordenadasDTO geocodificar(
            String direccion,
            String distrito,
            String ciudad) {

        if (!habilitada) {
            return null;
        }

        String consulta =
                direccion + ", " + distrito + ", " + ciudad;

        try {

            List<LugarNominatim> lugares =
                    restClient.get()
                            .uri(uri -> uri
                                    .path("/search")
                                    .queryParam("q", consulta)
                                    .queryParam("format", "json")
                                    .queryParam("limit", 1)
                                    .build())
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            List<LugarNominatim>>() {}
                            );

            if (lugares == null || lugares.isEmpty()) {
                log.warn(
                        "No se encontraron coordenadas para: "
                                + consulta
                );
                return null;
            }

            LugarNominatim lugar = lugares.get(0);

            double latitud = Double.parseDouble(lugar.lat());
            double longitud = Double.parseDouble(lugar.lon());

            return new CoordenadasDTO(
                    latitud,
                    longitud
            );

        } catch (RestClientException | NumberFormatException e) {

            log.warn(
                    "No se pudo geocodificar la dirección: "
                            + consulta
            );

            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LugarNominatim(String lat, String lon) {
    }
}
package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.repository.ComercioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    public static final double RADIO_MAXIMO_KM = 50;
    private static final double RADIO_TIERRA_KM = 6371;
    private static final double KM_POR_GRADO = 111.32;

    private final ComercioRepository comercioRepository;

    public List<Comercio> buscarCercanos(
            double latitud,
            double longitud,
            double radioKm) {

        validarUbicacion(latitud, longitud, radioKm);

        double deltaLatitud = radioKm / KM_POR_GRADO;

        double deltaLongitud = radioKm /
                (KM_POR_GRADO *
                        Math.max(Math.cos(Math.toRadians(latitud)), 0.01));

        return comercioRepository
                .findByEstadoAndLatitudBetweenAndLongitudBetween(
                        EstadoComercio.APPROVED,
                        latitud - deltaLatitud,
                        latitud + deltaLatitud,
                        longitud - deltaLongitud,
                        longitud + deltaLongitud
                );
    }

    public void validarUbicacion(
            double latitud,
            double longitud,
            double radioKm) {

        if (latitud < -90 || latitud > 90
                || longitud < -180 || longitud > 180) {

            throw new ReglaNegocioException(
                    "La latitud debe estar entre -90 y 90 " +
                    "y la longitud entre -180 y 180"
            );
        }

        if (radioKm <= 0 || radioKm > RADIO_MAXIMO_KM) {

            throw new ReglaNegocioException(
                    "El radio debe ser mayor que 0 y como maximo 50 km"
            );
        }
    }

    public double distanciaKm(
            double lat1,
            double lng1,
            double lat2,
            double lng2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLng / 2), 2);

        return 2 * RADIO_TIERRA_KM
                * Math.asin(Math.sqrt(a));
    }

    public double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }
}
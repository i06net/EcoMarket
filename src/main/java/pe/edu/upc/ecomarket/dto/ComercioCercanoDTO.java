package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioCercanoDTO {

    private ComercioRespuestaDTO comercio;
    private double distanciaKm;
}

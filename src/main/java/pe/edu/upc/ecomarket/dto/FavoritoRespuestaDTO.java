package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoRespuestaDTO {

    private Long id;
    private ComercioRespuestaDTO comercio;
    private LocalDateTime fechaRegistro;
}

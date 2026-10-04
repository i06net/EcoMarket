package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionDTO {

    private ProductoRespuestaDTO producto;
    private String motor;
    private String mensaje;
}

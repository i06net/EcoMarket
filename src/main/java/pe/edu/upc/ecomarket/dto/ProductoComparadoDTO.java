package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoComparadoDTO {

    private ProductoRespuestaDTO producto;
    private Double distanciaKm;
}

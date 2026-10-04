package pe.edu.upc.ecomarket.dto;

import lombok.Data;
import pe.edu.upc.ecomarket.models.EstadoComercio;

@Data
public class ValidacionComercioDTO {

    private EstadoComercio estado;
    private String motivo;
}
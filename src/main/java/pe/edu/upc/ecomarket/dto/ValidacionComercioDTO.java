package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.edu.upc.ecomarket.models.EstadoComercio;

@Data
public class ValidacionComercioDTO {

    @NotNull(message = "El estado es obligatorio")
    private EstadoComercio estado;

    private String motivo;
}
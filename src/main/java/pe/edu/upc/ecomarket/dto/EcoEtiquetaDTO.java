package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoEtiquetaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la Eco-Etiqueta es obligatorio")
    private String nombre;

    private String descripcion;
    private String criterio;
    private String palabrasClave;
    private Boolean activa;
}
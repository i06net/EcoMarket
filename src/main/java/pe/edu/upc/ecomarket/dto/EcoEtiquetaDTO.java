package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoEtiquetaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String criterio;
    private String palabrasClave;
    private Boolean activa;
}
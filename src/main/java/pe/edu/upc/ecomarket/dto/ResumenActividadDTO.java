package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumenActividadDTO {

    private List<ConteoDTO> productosConsultadosPorEcoEtiqueta;
    private long comerciosFavoritos;
    private String mensaje;
}

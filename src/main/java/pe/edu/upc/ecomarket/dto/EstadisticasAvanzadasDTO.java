package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasAvanzadasDTO {

    private String categoriaPrincipal;
    private double visitasPromedioPorProducto;
    private double visitasPromedioPorProductoCategoria;
    private List<ConteoDTO> evolucionMensual;
}

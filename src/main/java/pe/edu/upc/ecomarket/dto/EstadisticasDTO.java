package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstadisticasDTO {

    private Long comercioId;
    private String periodo;
    private LocalDate desde;
    private LocalDate hasta;
    private long visitasComercio;
    private List<ConteoDTO> visitasProductos;
    private Long totalVisitas;
    private List<ConteoDTO> terminosBusqueda;
    private String mensajeVisitas;
    private String mensajeBusquedas;
    private boolean premium;
    private EstadisticasAvanzadasDTO avanzadas;
    private String mensajePremium;
}

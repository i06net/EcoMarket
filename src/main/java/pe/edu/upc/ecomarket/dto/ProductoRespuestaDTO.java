package pe.edu.upc.ecomarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoRespuestaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private Long comercioId;
    private String comercioNombre;
    private Long categoriaId;
    private String categoriaNombre;
    private List<EcoEtiquetaDTO> ecoEtiquetas;
    private String motorClasificacion;
    private boolean destacado;
    private String avisoEcoEtiquetas;
}

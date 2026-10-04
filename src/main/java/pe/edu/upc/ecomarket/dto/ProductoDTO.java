package pe.edu.upc.ecomarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductoDTO {

    private Long comercioId;
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private Set<Long> ecoEtiquetaIds;
}
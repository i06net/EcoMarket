package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductoDTO {

    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private Set<Long> ecoEtiquetaIds;
}
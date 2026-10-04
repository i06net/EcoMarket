package pe.edu.upc.ecomarket.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PromocionDTO {

    private Long comercioId;
    private String titulo;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
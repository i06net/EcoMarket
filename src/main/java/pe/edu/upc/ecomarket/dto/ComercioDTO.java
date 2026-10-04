package pe.edu.upc.ecomarket.dto;

import lombok.Data;

@Data
public class ComercioDTO {

    private String nombre;
    private String descripcion;
    private String telefono;
    private String correoContacto;
    private String horario;
    private String direccion;
    private String distrito;
    private String ciudad;
    private Double latitud;
    private Double longitud;
}
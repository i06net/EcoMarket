package pe.edu.upc.ecomarket.dto;

import lombok.Data;

@Data
public class RegistroDTO {

    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String rol;
}
package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    // el correo y la contraseña son obligatorios para iniciar sesion
    @NotBlank
    private String correo;

    @NotBlank
    private String contrasena;
}
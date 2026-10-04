package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistroDTO {

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    // validamos que el correo tenga formato de correo
    @Email
    @NotBlank
    private String correo;

    // la contraseña minimo de 8 caracteres
    @Size(min = 8)
    private String contrasena;

    @NotBlank
    @Pattern(regexp = "CONSUMIDOR|COMERCIANTE")
    private String rol;
}
package pe.edu.upc.ecomarket.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ComercioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @Pattern(regexp = "[0-9]{9}", message = "El teléfono debe tener 9 números")
    private String telefono;

    @Email(message = "El correo no es válido")
    private String correoContacto;

    private String horario;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @DecimalMin("-90")
    @DecimalMax("90")
    private Double latitud;

    @DecimalMin("-180")
    @DecimalMax("180")
    private Double longitud;
}
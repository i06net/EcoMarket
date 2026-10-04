package pe.edu.upc.ecomarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private boolean activo;
    private LocalDateTime fechaRegistro;
}

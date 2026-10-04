package pe.edu.upc.ecomarket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ComercioRespuestaDTO {

    private Long id;
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
    private String estado;
    private String motivoRechazo;
    private Long propietarioId;
    private String propietarioNombre;
    private LocalDateTime fechaRegistro;
    private boolean premium;
    private List<PromocionRespuestaDTO> promociones;
}

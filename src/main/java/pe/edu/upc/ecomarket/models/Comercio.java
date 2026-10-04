package pe.edu.upc.ecomarket.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comercios")
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    private EstadoComercio estado = EstadoComercio.PENDING;

    private String motivoRechazo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
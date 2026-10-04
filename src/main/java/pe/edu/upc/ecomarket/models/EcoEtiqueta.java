package pe.edu.upc.ecomarket.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "eco_etiquetas")
public class EcoEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private String criterio;
    private String palabrasClave;
    private boolean activa = true;

    public EcoEtiqueta(String nombre, String descripcion, String criterio, String palabrasClave) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.criterio = criterio;
        this.palabrasClave = palabrasClave;
    }
}
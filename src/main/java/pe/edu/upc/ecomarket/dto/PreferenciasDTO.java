package pe.edu.upc.ecomarket.dto;

import lombok.Data;

import java.util.Set;

@Data
public class PreferenciasDTO {

    private Set<Long> ecoEtiquetaIds;
}
package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.EstadisticasDTO;
import pe.edu.upc.ecomarket.dto.ResumenActividadDTO;
import pe.edu.upc.ecomarket.services.EstadisticaService;

@RestController
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @PreAuthorize("hasAnyRole('COMERCIANTE', 'ADMINISTRADOR')")
    @GetMapping("/api/comercios/{id}/estadisticas")
    public EstadisticasDTO delComercio(
            @PathVariable Long id,
            @RequestParam(defaultValue = "mes") String periodo) {

        return estadisticaService.delComercio(
                id,
                periodo
        );
    }

    @PreAuthorize("hasRole('CONSUMIDOR')")
    @GetMapping("/api/consumidor/resumen")
    public ResumenActividadDTO resumen() {

        return estadisticaService.resumenConsumidor();
    }
}
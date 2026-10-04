package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.RecomendacionComerciosDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionProductosDTO;
import pe.edu.upc.ecomarket.services.RecomendacionService;

@RestController
@RequestMapping("/api/recomendaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONSUMIDOR')")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @GetMapping("/productos")
    public RecomendacionProductosDTO productos() {

        return recomendacionService.productos();
    }

    @GetMapping("/comercios")
    public RecomendacionComerciosDTO comercios(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radio) {

        return recomendacionService.comercios(
                lat,
                lng,
                radio
        );
    }
}
package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.PromocionDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionRespuestaDTO;
import pe.edu.upc.ecomarket.services.SuscripcionService;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMERCIANTE')")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @PostMapping("/api/suscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SuscripcionRespuestaDTO contratar(
            @Valid @RequestBody SuscripcionDTO dto) {

        return suscripcionService.contratar(dto);
    }

    @GetMapping("/api/suscripciones")
    public SuscripcionRespuestaDTO consultar(
            @RequestParam Long comercioId) {

        return suscripcionService.consultar(comercioId);
    }

    @PostMapping("/api/promociones")
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionRespuestaDTO crearPromocion(
            @Valid @RequestBody PromocionDTO dto) {

        return suscripcionService.crearPromocion(dto);
    }
}
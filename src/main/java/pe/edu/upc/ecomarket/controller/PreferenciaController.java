package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.dto.PreferenciasDTO;
import pe.edu.upc.ecomarket.services.PreferenciaService;

import java.util.List;

@RestController
@RequestMapping("/api/preferencias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONSUMIDOR')")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;

    @GetMapping
    public List<EcoEtiquetaDTO> listar() {

        return preferenciaService.listar();
    }

    @PutMapping
    public List<EcoEtiquetaDTO> guardar(
            @Valid @RequestBody PreferenciasDTO dto) {

        return preferenciaService.guardar(dto);
    }
}
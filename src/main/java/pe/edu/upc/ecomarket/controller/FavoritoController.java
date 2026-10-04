package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.FavoritoDTO;
import pe.edu.upc.ecomarket.dto.FavoritoRespuestaDTO;
import pe.edu.upc.ecomarket.services.FavoritoService;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONSUMIDOR')")
public class FavoritoController {

    private final FavoritoService favoritoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoritoRespuestaDTO agregar(
            @Valid @RequestBody FavoritoDTO dto) {

        return favoritoService.agregar(dto);
    }

    @GetMapping
    public List<FavoritoRespuestaDTO> listar() {

        return favoritoService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id) {

        favoritoService.eliminar(id);
    }
}
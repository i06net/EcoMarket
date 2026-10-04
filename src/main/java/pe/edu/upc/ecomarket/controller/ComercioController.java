package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.ComercioDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ValidacionComercioDTO;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.services.ActividadService;
import pe.edu.upc.ecomarket.services.ComercioService;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final ComercioService comercioService;
    private final ProductoService productoService;
    private final ActividadService actividadService;

    @PreAuthorize("hasRole('COMERCIANTE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComercioRespuestaDTO crear(
            @Valid @RequestBody ComercioDTO dto) {

        return comercioService.crear(dto);
    }

    @GetMapping
    public List<ComercioRespuestaDTO> listar(
            @RequestParam(required = false) String distrito) {

        return comercioService.listarAprobados(distrito);
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @GetMapping("/mis-comercios")
    public List<ComercioRespuestaDTO> misComercios() {

        return comercioService.misComercios();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/pendientes")
    public List<ComercioRespuestaDTO> pendientes() {

        return comercioService.pendientes();
    }

    @GetMapping("/{id}")
    public ComercioRespuestaDTO obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String busqueda) {

        Comercio comercio =
                comercioService.obtenerVisible(id);

        actividadService.registrarVisita(
                comercio,
                null,
                busqueda
        );

        return comercioService.aDTO(comercio);
    }

    @GetMapping("/{id}/productos")
    public List<ProductoRespuestaDTO> catalogo(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre) {

        return productoService.catalogo(id, nombre);
    }

    @PutMapping("/{id}")
    public ComercioRespuestaDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComercioDTO dto) {

        return comercioService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id) {

        comercioService.eliminar(id);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/validacion")
    public ComercioRespuestaDTO validar(
            @PathVariable Long id,
            @Valid @RequestBody ValidacionComercioDTO dto) {

        return comercioService.validar(id, dto);
    }
}
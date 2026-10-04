package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.services.ActividadService;
import pe.edu.upc.ecomarket.services.ComercioService;
import pe.edu.upc.ecomarket.services.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final ComercioService comercioService;
    private final ProductoService productoService;
    private final ActividadService actividadService;

    @GetMapping("/cercanos")
    public List<ComercioCercanoDTO> cercanos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radio) {

        return comercioService.cercanos(
                lat,
                lng,
                radio
        );
    }

    @GetMapping("/productos")
    public List<ProductoRespuestaDTO> productos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ecoEtiqueta) {

        actividadService.registrarBusqueda(
                nombre,
                Busqueda.TIPO_PRODUCTO
        );

        return productoService.buscarOrdenado(
                nombre,
                categoria,
                ecoEtiqueta
        );
    }

    @GetMapping("/comercios")
    public List<ComercioRespuestaDTO> comercios(
            @RequestParam String texto) {

        actividadService.registrarBusqueda(
                texto,
                Busqueda.TIPO_COMERCIO
        );

        return comercioService.buscarPorTexto(texto);
    }
}
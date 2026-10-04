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

import java.math.BigDecimal;
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
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ecoEtiqueta,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(defaultValue = "false") boolean soloDisponibles) {

        boolean buscaPorTexto = texto != null && !texto.isBlank();
        String termino = buscaPorTexto ? texto : nombre;

        actividadService.registrarBusqueda(
                termino,
                Busqueda.TIPO_PRODUCTO
        );

        if (buscaPorTexto) {
            return productoService.buscarOrdenadoPorTexto(
                    texto,
                    categoria,
                    ecoEtiqueta,
                    precioMin,
                    precioMax,
                    soloDisponibles
            );
        }

        return productoService.buscarOrdenado(
                nombre,
                categoria,
                ecoEtiqueta,
                precioMin,
                precioMax,
                soloDisponibles
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
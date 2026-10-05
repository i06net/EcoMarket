package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionComerciosDTO;
import pe.edu.upc.ecomarket.dto.RecomendacionProductosDTO;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.BusquedaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private static final int MAXIMO = 10;
    private static final String POR_PREFERENCIAS = "preferencias";

    private final PreferenciaService preferenciaService;
    private final ProductoService productoService;
    private final ComercioService comercioService;
    private final BusquedaRepository busquedaRepository;
    private final ProductoRepository productoRepository;
    private final VisitaRepository visitaRepository;
    private final UsuarioActual usuarioActual;

    public RecomendacionProductosDTO productos() {

        Usuario usuario = usuarioActual.obtener();

        Set<Long> preferidas =
                idsPreferidas(usuario);

        List<String> terminos =
                busquedaRepository
                        .findTop20ByUsuarioIdOrderByFechaDesc(
                                usuario.getId()
                        )
                        .stream()
                        .map(Busqueda::getTermino)
                        .distinct()
                        .toList();

        if (!preferidas.isEmpty() || !terminos.isEmpty()) {

            List<ProductoRespuestaDTO> recomendados =
                    productoService.buscar("", "", "")
                            .stream()
                            .filter(p ->
                                    puntaje(
                                            p,
                                            preferidas,
                                            terminos
                                    ) > 0
                            )
                            .sorted((a, b) ->
                                    Integer.compare(
                                            puntaje(
                                                    b,
                                                    preferidas,
                                                    terminos
                                            ),
                                            puntaje(
                                                    a,
                                                    preferidas,
                                                    terminos
                                            )
                                    )
                            )
                            .limit(MAXIMO)
                            .map(productoService::aDTO)
                            .toList();

            if (!recomendados.isEmpty()) {
                return new RecomendacionProductosDTO(
                        POR_PREFERENCIAS,
                        recomendados
                );
            }
        }

        return new RecomendacionProductosDTO(
                "mas-consultados",
                masConsultados()
        );
    }

    public RecomendacionComerciosDTO comercios(
            double latitud,
            double longitud,
            double radioKm) {

        Usuario usuario = usuarioActual.obtener();

        Set<Long> preferidas =
                idsPreferidas(usuario);

        List<ComercioCercanoDTO> cercanos =
                comercioService.cercanos(
                        latitud,
                        longitud,
                        radioKm
                );

        if (!preferidas.isEmpty()) {

            Map<Long, Long> afinidad =
                    new HashMap<>();

            for (ComercioCercanoDTO cercano : cercanos) {

                Long comercioId =
                        cercano.getComercio().getId();

                long cantidad =
                        productosAfines(
                                comercioId,
                                preferidas
                        );

                afinidad.put(comercioId, cantidad);
            }

            List<ComercioCercanoDTO> recomendados =
                    cercanos.stream()
                            .filter(c ->
                                    afinidad.get(
                                            c.getComercio().getId()
                                    ) > 0
                            )
                            .sorted((a, b) ->
                                    Long.compare(
                                            afinidad.get(
                                                    b.getComercio().getId()
                                            ),
                                            afinidad.get(
                                                    a.getComercio().getId()
                                            )
                                    )
                            )
                            .limit(MAXIMO)
                            .toList();

            if (!recomendados.isEmpty()) {
                return new RecomendacionComerciosDTO(
                        POR_PREFERENCIAS,
                        recomendados
                );
            }
        }

        return new RecomendacionComerciosDTO(
                "mas-cercanos",
                cercanos.stream()
                        .limit(MAXIMO)
                        .toList()
        );
    }

    private Set<Long> idsPreferidas(Usuario usuario) {

        return preferenciaService
                .ecoEtiquetasDe(usuario.getId())
                .stream()
                .map(EcoEtiqueta::getId)
                .collect(Collectors.toSet());
    }

    private static int puntaje(
            Producto producto,
            Set<Long> preferidas,
            List<String> terminos) {

        int puntos = 0;

        for (EcoEtiqueta etiqueta :
                producto.getEcoEtiquetas()) {

            if (preferidas.contains(etiqueta.getId())) {
                puntos += 2;
            }
        }

        String descripcion =
                producto.getDescripcion();

        if (descripcion == null) {
            descripcion = "";
        }

        String texto =
                (producto.getNombre() + " " + descripcion)
                        .toLowerCase();

        for (String termino : terminos) {

                if (texto.contains(termino)) {
                        puntos++;
                }
        }

        return puntos;
    }

    private long productosAfines(
            Long comercioId,
            Set<Long> preferidas) {

        List<Producto> productos =
                productoRepository
                        .findByComercioIdAndActivoTrueOrderByNombreAsc(
                                comercioId
                        );

        long cantidad = 0;

        for (Producto producto : productos) {

            for (EcoEtiqueta etiqueta :
                    producto.getEcoEtiquetas()) {

                if (preferidas.contains(etiqueta.getId())) {
                    cantidad++;
                    break;
                }
            }
        }

        return cantidad;
    }

    private List<ProductoRespuestaDTO> masConsultados() {

        List<ProductoRespuestaDTO> resultado =
                new ArrayList<>();

        for (ConteoDTO conteo :
                visitaRepository.productosMasConsultados()) {

            if (resultado.size() == MAXIMO) {
                break;
            }

            Producto producto =
                    productoRepository
                            .findById(conteo.getId())
                            .orElse(null);

            if (producto != null
                    && !producto.getEcoEtiquetas().isEmpty()) {

                resultado.add(
                        productoService.aDTO(producto)
                );
            }
        }

        if (!resultado.isEmpty()) {
            return resultado;
        }

        return productoService
                .buscar("", "", "")
                .stream()
                .filter(p ->
                        !p.getEcoEtiquetas().isEmpty()
                )
                .sorted(
                        Comparator.comparing(
                                Producto::getFechaRegistro
                        ).reversed()
                )
                .limit(MAXIMO)
                .map(productoService::aDTO)
                .toList();
    }
}
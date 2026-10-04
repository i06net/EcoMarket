package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.dto.EstadisticasAvanzadasDTO;
import pe.edu.upc.ecomarket.dto.EstadisticasDTO;
import pe.edu.upc.ecomarket.dto.ResumenActividadDTO;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Categoria;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.models.Visita;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private static final int MESES_EVOLUCION = 6;

    private final ComercioService comercioService;
    private final VisitaRepository visitaRepository;
    private final ProductoRepository productoRepository;
    private final FavoritoRepository favoritoRepository;
    private final UsuarioActual usuarioActual;
    private final UbicacionService ubicacionService;

    public EstadisticasDTO delComercio(
            Long comercioId,
            String periodo) {

        Comercio comercio = comercioService.buscarPropio(
                comercioId,
                "Solo puedes ver las estadísticas de tus propios comercios"
        );

        LocalDate hasta = LocalDate.now();
        LocalDate desde;

        if (periodo.equals("semana")) {
            desde = hasta.minusDays(6);
        } else if (periodo.equals("mes")) {
            desde = hasta.minusDays(29);
        } else if (periodo.equals("anio")) {
            desde = hasta.minusDays(364);
        } else {
            throw new ReglaNegocioException(
                    "El periodo debe ser semana, mes o anio"
            );
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        EstadisticasDTO dto = new EstadisticasDTO();

        dto.setComercioId(comercio.getId());
        dto.setPeriodo(periodo);
        dto.setDesde(desde);
        dto.setHasta(hasta);

        dto.setVisitasComercio(
                visitaRepository
                        .countByComercioIdAndProductoIsNullAndFechaBetween(
                                comercio.getId(),
                                inicio,
                                fin
                        )
        );

        dto.setVisitasProductos(
                visitaRepository.contarPorProducto(
                        comercio.getId(),
                        inicio,
                        fin
                )
        );

        dto.setTerminosBusqueda(
                visitaRepository
                        .contarTerminos(comercio.getId(), inicio, fin)
                        .stream()
                        .limit(10)
                        .toList()
        );

        if (dto.getVisitasComercio() == 0
                && dto.getVisitasProductos().isEmpty()) {

            dto.setMensajeVisitas(
                    "Aún no hay visitas en este periodo"
            );
        }

        if (dto.getTerminosBusqueda().isEmpty()) {
            dto.setMensajeBusquedas(
                    "Aún no hay búsquedas registradas que lleven a tu comercio"
            );
        }

        dto.setPremium(
                comercioService.esPremium(comercio.getId())
        );

        if (dto.isPremium()) {
            dto.setAvanzadas(
                    avanzadas(comercio, inicio, fin)
            );
        } else {
            dto.setMensajePremium(
                    "Contrata el plan premium para ver la comparación " +
                    "con tu categoría y la evolución mensual"
            );
        }

        return dto;
    }

    private EstadisticasAvanzadasDTO avanzadas(
            Comercio comercio,
            LocalDateTime inicio,
            LocalDateTime fin) {

        List<Producto> productos =
                productoRepository
                        .findByComercioIdAndActivoTrueOrderByNombreAsc(
                                comercio.getId()
                        );

        Map<Long, Integer> productosPorCategoria =
                new HashMap<>();

        Categoria categoriaPrincipal = null;
        int mayorCantidad = 0;

        for (Producto producto : productos) {

            Long categoriaId = producto.getCategoria().getId();

            int cantidad = productosPorCategoria
                    .getOrDefault(categoriaId, 0) + 1;

            productosPorCategoria.put(categoriaId, cantidad);

            if (cantidad > mayorCantidad) {
                mayorCantidad = cantidad;
                categoriaPrincipal = producto.getCategoria();
            }
        }

        long visitasPropias = visitaRepository
                .contarPorProducto(
                        comercio.getId(),
                        inicio,
                        fin
                )
                .stream()
                .mapToLong(ConteoDTO::getCantidad)
                .sum();

        double promedioPropio = productos.isEmpty()
                ? 0
                : (double) visitasPropias / productos.size();

        double promedioCategoria = 0;

        if (categoriaPrincipal != null) {

            long productosCategoria =
                    productoRepository
                            .countByCategoriaIdAndActivoTrueAndComercioEstado(
                                    categoriaPrincipal.getId(),
                                    EstadoComercio.APPROVED
                            );

            long visitasCategoria =
                    visitaRepository.contarPorCategoria(
                            categoriaPrincipal.getId(),
                            inicio,
                            fin
                    );

            if (productosCategoria > 0) {
                promedioCategoria =
                        (double) visitasCategoria / productosCategoria;
            }
        }

        LocalDateTime inicioEvolucion =
                YearMonth.now()
                        .minusMonths(MESES_EVOLUCION - 1)
                        .atDay(1)
                        .atStartOfDay();

        List<Visita> visitas =
                visitaRepository.findByComercioIdAndFechaAfter(
                        comercio.getId(),
                        inicioEvolucion
                );

        List<ConteoDTO> evolucion = new ArrayList<>();

        for (int i = MESES_EVOLUCION - 1; i >= 0; i--) {

            YearMonth mes =
                    YearMonth.now().minusMonths(i);

            long cantidad = 0;

            for (Visita visita : visitas) {

                if (YearMonth.from(visita.getFecha()).equals(mes)) {
                    cantidad++;
                }
            }

            evolucion.add(
                    new ConteoDTO(
                            null,
                            mes.toString(),
                            cantidad
                    )
            );
        }

        String nombreCategoria =
                categoriaPrincipal == null
                        ? null
                        : categoriaPrincipal.getNombre();

        return new EstadisticasAvanzadasDTO(
                nombreCategoria,
                ubicacionService.redondear(promedioPropio),
                ubicacionService.redondear(promedioCategoria),
                evolucion
        );
    }

    public ResumenActividadDTO resumenConsumidor() {

        Usuario usuario = usuarioActual.obtener();

        Map<Long, Producto> productosConsultados =
                new HashMap<>();

        List<Visita> visitas =
                visitaRepository.findByUsuarioIdAndProductoIsNotNull(
                        usuario.getId()
                );

        for (Visita visita : visitas) {

            Producto producto = visita.getProducto();

            productosConsultados.put(
                    producto.getId(),
                    producto
            );
        }

        Map<String, Long> etiquetas =
                new TreeMap<>();

        for (Producto producto : productosConsultados.values()) {

            for (EcoEtiqueta etiqueta : producto.getEcoEtiquetas()) {

                etiquetas.merge(
                        etiqueta.getNombre(),
                        1L,
                        Long::sum
                );
            }
        }

        List<ConteoDTO> conteo = new ArrayList<>();

        for (Map.Entry<String, Long> entrada
                : etiquetas.entrySet()) {

            conteo.add(
                    new ConteoDTO(
                            null,
                            entrada.getKey(),
                            entrada.getValue()
                    )
            );
        }

        conteo.sort(
                Comparator.comparing(
                        ConteoDTO::getCantidad
                ).reversed()
        );

        long favoritos =
                favoritoRepository.countByUsuarioId(
                        usuario.getId()
                );

        String mensaje = null;

        if (productosConsultados.isEmpty() && favoritos == 0) {
            mensaje =
                    "Aún no tienes actividad. Explora los comercios " +
                    "sostenibles cercanos a ti";
        }

        return new ResumenActividadDTO(
                conteo,
                favoritos,
                mensaje
        );
    }
}
package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.ClasificacionDTO;
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.dto.ProductoComparadoDTO;
import pe.edu.upc.ecomarket.dto.ProductoDTO;
import pe.edu.upc.ecomarket.dto.ProductoRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.EcoEtiquetaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoService {

    public static final String MOTOR_MANUAL = "manual";

    private final ProductoRepository productoRepository;
    private final ComercioService comercioService;
    private final CategoriaService categoriaService;
    private final EcoEtiquetaService ecoEtiquetaService;
    private final UsuarioActual usuarioActual;
    private final ClasificadorService clasificadorService;
    private final EcoEtiquetaRepository ecoEtiquetaRepository;
    private final PreferenciaService preferenciaService;

    @Transactional
    public ProductoRespuestaDTO crear(ProductoDTO dto) {

        Comercio comercio =
                comercioService.buscar(dto.getComercioId());

        Usuario usuario = usuarioActual.obtener();

        if (!comercio.getPropietario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException(
                    "Solo puedes publicar productos en tus propios comercios"
            );
        }

        Producto producto = new Producto();
        producto.setComercio(comercio);

        copiarDatos(dto, producto);

        if (dto.getEcoEtiquetaIds() == null) {
            aplicarClasificacion(producto);
        }

        return aDTO(productoRepository.save(producto));
    }

    @Transactional
    public ClasificacionDTO clasificar(Long id) {

        Producto producto = buscarPropio(
                id,
                "Solo puedes clasificar tus propios productos"
        );

        String motor = aplicarClasificacion(producto);

        productoRepository.save(producto);

        String mensaje;

        if (producto.getEcoEtiquetas().isEmpty()) {
            mensaje =
                    "La descripción no menciona características sostenibles: " +
                    "no se asignaron Eco-Etiquetas";
        } else {
            mensaje =
                    "Se asignaron " +
                    producto.getEcoEtiquetas().size() +
                    " Eco-Etiquetas";
        }

        return new ClasificacionDTO(
                aDTO(producto),
                motor,
                mensaje
        );
    }

    private String aplicarClasificacion(Producto producto) {

        String descripcion = producto.getDescripcion();

        if (descripcion == null) {
            descripcion = "";
        }

        String texto =
                producto.getNombre() + ". " + descripcion;

        List<EcoEtiqueta> etiquetas =
                ecoEtiquetaRepository
                        .findByActivaTrueOrderByNombreAsc();

        ClasificadorService.Resultado resultado =
                clasificadorService.clasificar(
                        texto,
                        etiquetas
                );

        producto.setEcoEtiquetas(
                new HashSet<>(resultado.ecoEtiquetas())
        );

        producto.setMotorClasificacion(
                resultado.motor()
        );

        return resultado.motor();
    }

    public Producto obtenerVisible(Long id) {

        Producto producto = productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto",
                                id
                        ));

        Comercio comercio = producto.getComercio();

        if (comercio.getEstado() != EstadoComercio.APPROVED
                && !comercioService.puedeAdministrar(comercio)) {

            throw new RecursoNoEncontradoException(
                    "Producto",
                    id
            );
        }

        return producto;
    }

    @Transactional
    public ProductoRespuestaDTO actualizar(
            Long id,
            ProductoDTO dto) {

        Producto producto = buscarPropio(
                id,
                "Solo puedes editar tus propios productos"
        );

        if (!producto.getComercio().getId()
                .equals(dto.getComercioId())) {

            throw new ReglaNegocioException(
                    "El comercio de un producto no se puede cambiar"
            );
        }

        copiarDatos(dto, producto);

        return aDTO(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {

        Producto producto = buscarPropio(
                id,
                "Solo puedes eliminar tus propios productos"
        );

        producto.setActivo(false);
        productoRepository.save(producto);
    }

        @Transactional(readOnly = true)
        public List<ProductoRespuestaDTO> catalogo(
            Long comercioId,
            String nombre) {

        Comercio comercio =
                comercioService.obtenerVisible(comercioId);

        return productoRepository
                .findByComercioIdAndActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(
                        comercio.getId(),
                        texto(nombre)
                )
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<ProductoRespuestaDTO> misProductos() {

        Long usuarioId = usuarioActual.obtener().getId();

        return productoRepository
                .findByComercioPropietarioIdAndActivoTrueOrderByIdAsc(
                        usuarioId
                )
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<Producto> buscar(
            String nombre,
            String categoria,
            String ecoEtiqueta) {

        return productoRepository.buscar(
                texto(nombre),
                texto(categoria),
                texto(ecoEtiqueta)
        );
    }

        @Transactional(readOnly = true)
        public List<ProductoRespuestaDTO> buscarOrdenado(
            String nombre,
            String categoria,
            String ecoEtiqueta) {

        return ordenar(
                buscar(nombre, categoria, ecoEtiqueta)
        );
    }

        @Transactional(readOnly = true)
        public List<ProductoRespuestaDTO> buscarOrdenadoPorTexto(
            String texto,
            String categoria,
            String ecoEtiqueta) {

        return ordenar(
                productoRepository.buscarPorTexto(
                        texto(texto),
                        texto(categoria),
                        texto(ecoEtiqueta)
                )
        );
    }

    private List<ProductoRespuestaDTO> ordenar(
            List<Producto> productos) {

        Set<Long> idsPreferidos = new HashSet<>();

        Usuario usuario = usuarioActual.buscar();

        if (usuario != null) {

            for (EcoEtiqueta etiqueta :
                    preferenciaService.ecoEtiquetasDe(usuario.getId())) {

                idsPreferidos.add(etiqueta.getId());
            }
        }

        List<ProductoRespuestaDTO> resultado =
                new ArrayList<>();

        for (Producto producto : productos) {

            resultado.add(aDTO(producto));
        }

        resultado.sort((a, b) -> {

            if (a.isDestacado() != b.isDestacado()) {

                if (a.isDestacado()) {
                    return -1;
                }

                return 1;
            }

            long coincidenciasA =
                    coincidencias(a, idsPreferidos);

            long coincidenciasB =
                    coincidencias(b, idsPreferidos);

            return Long.compare(
                    coincidenciasB,
                    coincidenciasA
            );
        });

        return resultado;
    }

    private long coincidencias(
            ProductoRespuestaDTO producto,
            Set<Long> idsPreferidos) {

        long total = 0;

        for (EcoEtiquetaDTO etiqueta :
                producto.getEcoEtiquetas()) {

            if (idsPreferidos.contains(etiqueta.getId())) {
                total++;
            }
        }

        return total;
    }

        @Transactional(readOnly = true)
        public List<ProductoComparadoDTO> comparar(
            List<Long> ids,
            Double latitud,
            Double longitud) {

        Set<Long> idsDistintos =
                new HashSet<>(ids);

        if (idsDistintos.size() < 2
                || idsDistintos.size() > 4) {

            throw new ReglaNegocioException(
                    "Debes elegir entre 2 y 4 productos distintos para compararlos"
            );
        }

        if ((latitud == null) != (longitud == null)) {

            throw new ReglaNegocioException(
                    "Envía la latitud y la longitud juntas, o ninguna de las dos"
            );
        }

        List<Producto> productos =
                new ArrayList<>();

        for (Long id : idsDistintos) {
            productos.add(obtenerVisible(id));
        }

        productos.sort((a, b) ->
                a.getPrecio().compareTo(b.getPrecio())
        );

        List<ProductoComparadoDTO> resultado =
                new ArrayList<>();

        for (Producto producto : productos) {

            Double distancia = null;

            if (latitud != null) {

                Comercio comercio =
                        producto.getComercio();

                distancia = ComercioService.redondear(
                        ComercioService.distanciaKm(
                                latitud,
                                longitud,
                                comercio.getLatitud(),
                                comercio.getLongitud()
                        )
                );
            }

            resultado.add(
                    new ProductoComparadoDTO(
                            aDTO(producto),
                            distancia
                    )
            );
        }

        return resultado;
    }

    public Producto buscarPropio(
            Long id,
            String mensajeSiNoEsDueno) {

        Producto producto =
                productoRepository.findById(id)
                        .filter(Producto::isActivo)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Producto",
                                        id
                                ));

        comercioService.buscarPropio(
                producto.getComercio().getId(),
                mensajeSiNoEsDueno
        );

        return producto;
    }

    private void copiarDatos(
            ProductoDTO dto,
            Producto producto) {

        producto.setCategoria(
                categoriaService.buscar(dto.getCategoriaId())
        );

        producto.setNombre(
                dto.getNombre().trim()
        );

        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagenUrl(dto.getImagenUrl());

        if (dto.getEcoEtiquetaIds() != null) {

            producto.setEcoEtiquetas(
                    ecoEtiquetasActivas(
                            dto.getEcoEtiquetaIds()
                    )
            );

            producto.setMotorClasificacion(
                    MOTOR_MANUAL
            );
        }
    }

    private Set<EcoEtiqueta> ecoEtiquetasActivas(
            Set<Long> ids) {

        Set<EcoEtiqueta> etiquetas =
                new HashSet<>();

        for (Long id : ids) {

            EcoEtiqueta etiqueta =
                    ecoEtiquetaService.buscar(id);

            if (!etiqueta.isActiva()) {

                throw new ReglaNegocioException(
                        "La Eco-Etiqueta " +
                        etiqueta.getNombre() +
                        " está desactivada"
                );
            }

            etiquetas.add(etiqueta);
        }

        return etiquetas;
    }

    private static String texto(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.trim();
    }

    public ProductoRespuestaDTO aDTO(
            Producto producto) {

        ProductoRespuestaDTO dto =
                new ProductoRespuestaDTO();

        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setImagenUrl(producto.getImagenUrl());

        dto.setComercioId(
                producto.getComercio().getId()
        );

        dto.setComercioNombre(
                producto.getComercio().getNombre()
        );

        dto.setCategoriaId(
                producto.getCategoria().getId()
        );

        dto.setCategoriaNombre(
                producto.getCategoria().getNombre()
        );

        dto.setEcoEtiquetas(
                producto.getEcoEtiquetas()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        EcoEtiqueta::getNombre
                                )
                        )
                        .map(ecoEtiquetaService::aDTO)
                        .toList()
        );

        dto.setMotorClasificacion(
                producto.getMotorClasificacion()
        );

        dto.setDestacado(
                comercioService.esPremium(
                        producto.getComercio().getId()
                )
        );

        if (producto.getEcoEtiquetas().isEmpty()) {

            dto.setAvisoEcoEtiquetas(
                    "El producto aún no tiene Eco-Etiquetas"
            );
        }

        return dto;
    }
}
package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.ComercioCercanoDTO;
import pe.edu.upc.ecomarket.dto.ComercioDTO;
import pe.edu.upc.ecomarket.dto.ComercioRespuestaDTO;
import pe.edu.upc.ecomarket.dto.CoordenadasDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.ValidacionComercioDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Promocion;
import pe.edu.upc.ecomarket.models.Suscripcion;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.ComercioRepository;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;
import pe.edu.upc.ecomarket.repository.PromocionRepository;
import pe.edu.upc.ecomarket.repository.SuscripcionRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComercioService {

    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final FavoritoRepository favoritoRepository;
    private final VisitaRepository visitaRepository;
    private final PromocionRepository promocionRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final GeocodificacionService geocodificacionService;
    private final UsuarioActual usuarioActual;
    private final UbicacionService ubicacionService;

    @Transactional
    public ComercioRespuestaDTO crear(ComercioDTO dto) {

        Comercio comercio = new Comercio();
        comercio.setPropietario(usuarioActual.obtener());

        copiarDatos(dto, comercio);

        return aDTO(comercioRepository.save(comercio));
    }

    public List<ComercioRespuestaDTO> listarAprobados(String distrito) {

        List<Comercio> comercios;

        if (distrito == null || distrito.isBlank()) {
            comercios = comercioRepository
                    .findByEstadoOrderByIdAsc(EstadoComercio.APPROVED);
        } else {
            comercios = comercioRepository
                    .findByEstadoAndDistritoIgnoreCaseOrderByIdAsc(
                            EstadoComercio.APPROVED,
                            distrito.trim()
                    );
        }

        return comercios.stream()
                .map(this::aDTO)
                .toList();
    }

    public Comercio obtenerVisible(Long id) {

        Comercio comercio = buscar(id);

        if (comercio.getEstado() != EstadoComercio.APPROVED
                && !puedeAdministrar(comercio)) {
            throw new RecursoNoEncontradoException("Comercio", id);
        }

        return comercio;
    }

    public List<ComercioRespuestaDTO> buscarPorTexto(String texto) {

        return comercioRepository
                .findByEstadoAndNombreContainingIgnoreCaseOrderByNombreAsc(
                        EstadoComercio.APPROVED,
                        texto.trim()
                )
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<ComercioRespuestaDTO> misComercios() {

        Long usuarioId = usuarioActual.obtener().getId();

        return comercioRepository
                .findByPropietarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<ComercioRespuestaDTO> pendientes() {

        return comercioRepository
                .findByEstadoOrderByIdAsc(EstadoComercio.PENDING)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional
    public ComercioRespuestaDTO actualizar(Long id, ComercioDTO dto) {

        Comercio comercio = buscarPropio(
                id,
                "Solo puedes editar tus propios comercios"
        );

        copiarDatos(dto, comercio);

        if (comercio.getEstado() == EstadoComercio.REJECTED) {
            comercio.setEstado(EstadoComercio.PENDING);
            comercio.setMotivoRechazo(null);
        }

        return aDTO(comercioRepository.save(comercio));
    }

    @Transactional
    public void eliminar(Long id) {

        Comercio comercio = buscarPropio(
                id,
                "Solo puedes eliminar tus propios comercios"
        );

        visitaRepository.deleteByComercioId(id);
        favoritoRepository.deleteByComercioId(id);
        promocionRepository.deleteByComercioId(id);
        suscripcionRepository.deleteByComercioId(id);
        productoRepository.deleteAll(
                productoRepository.findByComercioId(id)
        );

        comercioRepository.delete(comercio);
    }

    @Transactional
    public ComercioRespuestaDTO validar(
            Long id,
            ValidacionComercioDTO dto) {

        Comercio comercio = buscar(id);

        if (dto.getEstado() == EstadoComercio.PENDING) {
            throw new ReglaNegocioException(
                    "El estado debe ser APPROVED o REJECTED"
            );
        }

        if (dto.getEstado() == EstadoComercio.REJECTED
                && (dto.getMotivo() == null || dto.getMotivo().isBlank())) {
            throw new ReglaNegocioException(
                    "Debes indicar el motivo del rechazo"
            );
        }

        comercio.setEstado(dto.getEstado());

        if (dto.getEstado() == EstadoComercio.REJECTED) {
            comercio.setMotivoRechazo(dto.getMotivo().trim());
        } else {
            comercio.setMotivoRechazo(null);
        }

        return aDTO(comercioRepository.save(comercio));
    }

    public List<ComercioCercanoDTO> cercanos(
            double latitud,
            double longitud,
            double radioKm) {

        List<Comercio> candidatos =
                ubicacionService.buscarCercanos(
                        latitud,
                        longitud,
                        radioKm
                );

        return candidatos.stream()
                .map(comercio -> {

                    double distancia = ubicacionService.redondear(
                            ubicacionService.distanciaKm(
                                    latitud,
                                    longitud,
                                    comercio.getLatitud(),
                                    comercio.getLongitud()
                            )
                    );

                    return new ComercioCercanoDTO(
                            aDTO(comercio),
                            distancia
                    );
                })
                .filter(comercio ->
                        comercio.getDistanciaKm() <= radioKm)
                .sorted(
                        java.util.Comparator.comparingDouble(
                                ComercioCercanoDTO::getDistanciaKm
                        )
                )
                .toList();
    }

    /*
     * Se mantiene este método para compatibilidad
     * con otros módulos que puedan utilizarlo.
     */
    public void validarUbicacion(
            double latitud,
            double longitud,
            double radioKm) {

        ubicacionService.validarUbicacion(
                latitud,
                longitud,
                radioKm
        );
    }

    /*
     * Se mantiene este método para compatibilidad
     * con otros módulos que puedan utilizarlo.
     */
    public static double distanciaKm(
            double lat1,
            double lng1,
            double lat2,
            double lng2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLng / 2), 2);

        return 2 * 6371
                * Math.asin(Math.sqrt(a));
    }

    /*
     * Se mantiene este método para compatibilidad
     * con otros módulos que puedan utilizarlo.
     */
    public static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    public boolean esPremium(Long comercioId) {

        return suscripcionRepository
                .findByComercioIdOrderByFechaFinDesc(comercioId)
                .stream()
                .anyMatch(Suscripcion::estaActiva);
    }

    public List<PromocionRespuestaDTO> promocionesVigentes(
            Long comercioId) {

        return promocionRepository
                .findByComercioIdOrderByFechaInicioAsc(comercioId)
                .stream()
                .filter(Promocion::estaVigente)
                .map(p -> new PromocionRespuestaDTO(
                        p.getId(),
                        comercioId,
                        p.getTitulo(),
                        p.getDescripcion(),
                        p.getFechaInicio(),
                        p.getFechaFin()
                ))
                .toList();
    }

    public Comercio buscar(Long id) {

        return comercioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Comercio",
                                id
                        ));
    }

    public boolean puedeAdministrar(Comercio comercio) {

        Usuario usuario = usuarioActual.buscar();

        if (usuario == null) {
            return false;
        }

        return UsuarioActual.esAdministrador(usuario)
                || usuario.getId().equals(
                        comercio.getPropietario().getId()
                );
    }

    public Comercio buscarPropio(
            Long id,
            String mensajeSiNoEsDueno) {

        Comercio comercio = buscar(id);
        Usuario usuario = usuarioActual.obtener();

        if (!UsuarioActual.esAdministrador(usuario)
                && !usuario.getId().equals(
                        comercio.getPropietario().getId())) {

            throw new AccesoDenegadoException(mensajeSiNoEsDueno);
        }

        return comercio;
    }

    private void copiarDatos(
            ComercioDTO dto,
            Comercio comercio) {

        validarCoordenadas(dto);

        comercio.setNombre(dto.getNombre().trim());
        comercio.setDescripcion(dto.getDescripcion());
        comercio.setTelefono(dto.getTelefono());
        comercio.setCorreoContacto(dto.getCorreoContacto());
        comercio.setHorario(dto.getHorario());
        comercio.setDireccion(dto.getDireccion().trim());
        comercio.setDistrito(dto.getDistrito().trim());
        comercio.setCiudad(dto.getCiudad().trim());

        resolverCoordenadas(dto, comercio);
    }

    private void validarCoordenadas(ComercioDTO dto) {

        if ((dto.getLatitud() == null)
                != (dto.getLongitud() == null)) {

            throw new ReglaNegocioException(
                    "Envía la latitud y la longitud juntas, o ninguna de las dos"
            );
        }
    }

    private void resolverCoordenadas(
            ComercioDTO dto,
            Comercio comercio) {

        if (dto.getLatitud() != null) {

            comercio.setLatitud(dto.getLatitud());
            comercio.setLongitud(dto.getLongitud());

            return;
        }

        CoordenadasDTO coordenadas =
                geocodificacionService.geocodificar(
                        dto.getDireccion(),
                        dto.getDistrito(),
                        dto.getCiudad()
                );

        if (coordenadas == null) {
            throw new ReglaNegocioException(
                    "No se pudo ubicar la dirección. " +
                    "Indica la latitud y la longitud del comercio en el mapa"
            );
        }

        comercio.setLatitud(coordenadas.getLatitud());
        comercio.setLongitud(coordenadas.getLongitud());
    }

    public ComercioRespuestaDTO aDTO(Comercio comercio) {

        ComercioRespuestaDTO dto = new ComercioRespuestaDTO();

        dto.setId(comercio.getId());
        dto.setNombre(comercio.getNombre());
        dto.setDescripcion(comercio.getDescripcion());
        dto.setTelefono(comercio.getTelefono());
        dto.setCorreoContacto(comercio.getCorreoContacto());
        dto.setHorario(comercio.getHorario());
        dto.setDireccion(comercio.getDireccion());
        dto.setDistrito(comercio.getDistrito());
        dto.setCiudad(comercio.getCiudad());
        dto.setLatitud(comercio.getLatitud());
        dto.setLongitud(comercio.getLongitud());
        dto.setEstado(comercio.getEstado().name());
        dto.setMotivoRechazo(comercio.getMotivoRechazo());
        dto.setPropietarioId(comercio.getPropietario().getId());
        dto.setPropietarioNombre(
                comercio.getPropietario().getNombres()
                        + " "
                        + comercio.getPropietario().getApellidos()
        );
        dto.setFechaRegistro(comercio.getFechaRegistro());
        dto.setPremium(esPremium(comercio.getId()));
        dto.setPromociones(promocionesVigentes(comercio.getId()));

        return dto;
    }
}
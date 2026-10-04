package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.PromocionDTO;
import pe.edu.upc.ecomarket.dto.PromocionRespuestaDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionDTO;
import pe.edu.upc.ecomarket.dto.SuscripcionRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Promocion;
import pe.edu.upc.ecomarket.models.Suscripcion;
import pe.edu.upc.ecomarket.repository.PromocionRepository;
import pe.edu.upc.ecomarket.repository.SuscripcionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    public static final BigDecimal PRECIO_MENSUAL =
            new BigDecimal("29.90");

    private final SuscripcionRepository suscripcionRepository;
    private final PromocionRepository promocionRepository;
    private final ComercioService comercioService;

    @Transactional
    public SuscripcionRespuestaDTO contratar(
            SuscripcionDTO dto) {

        Comercio comercio = comercioService.buscarPropio(
                dto.getComercioId(),
                "Solo puedes contratar planes para tus propios comercios"
        );

        if (comercio.getEstado() != EstadoComercio.APPROVED) {
            throw new ReglaNegocioException(
                    "El comercio debe estar aprobado para contratar el plan premium"
            );
        }

        BigDecimal monto =
                PRECIO_MENSUAL.multiply(
                        BigDecimal.valueOf(dto.getMeses())
                );

        Suscripcion actual =
                suscripcionActiva(comercio.getId());

        if (actual != null) {

            actual.setFechaFin(
                    actual.getFechaFin()
                            .plusMonths(dto.getMeses())
            );

            actual.setMonto(
                    actual.getMonto().add(monto)
            );

            return aDTO(
                    suscripcionRepository.save(actual)
            );
        }

        LocalDate hoy = LocalDate.now();

        Suscripcion suscripcion =
                new Suscripcion();

        suscripcion.setComercio(comercio);
        suscripcion.setMonto(monto);
        suscripcion.setFechaInicio(hoy);
        suscripcion.setFechaFin(
                hoy.plusMonths(dto.getMeses()).minusDays(1)
        );

        return aDTO(
                suscripcionRepository.save(suscripcion)
        );
    }

    public SuscripcionRespuestaDTO consultar(
            Long comercioId) {

        comercioService.buscarPropio(
                comercioId,
                "Solo puedes consultar los planes de tus propios comercios"
        );

        List<Suscripcion> suscripciones =
                suscripcionRepository
                        .findByComercioIdOrderByFechaFinDesc(
                                comercioId
                        );

        if (suscripciones.isEmpty()) {

            SuscripcionRespuestaDTO dto =
                    new SuscripcionRespuestaDTO();

            dto.setComercioId(comercioId);
            dto.setPlan("GRATUITO");
            dto.setMensaje(
                    "El comercio usa el plan gratuito. " +
                    "Puedes contratar el plan premium"
            );

            return dto;
        }

        SuscripcionRespuestaDTO dto =
                aDTO(suscripciones.get(0));

        if (!dto.isActiva()) {
            dto.setMensaje(
                    "Tu plan premium venció y sus funciones " +
                    "se desactivaron. Puedes renovarlo"
            );
        }

        return dto;
    }

    @Transactional
    public PromocionRespuestaDTO crearPromocion(
            PromocionDTO dto) {

        Comercio comercio =
                comercioService.buscarPropio(
                        dto.getComercioId(),
                        "Solo puedes publicar promociones en tus propios comercios"
                );

        if (!comercioService.esPremium(comercio.getId())) {
            throw new AccesoDenegadoException(
                    "Necesitas un plan premium activo para publicar promociones"
            );
        }

        if (dto.getFechaFin()
                .isBefore(dto.getFechaInicio())) {

            throw new ReglaNegocioException(
                    "La fecha de fin (fechaFin) no puede ser anterior " +
                    "a la fecha de inicio"
            );
        }

        Promocion promocion =
                new Promocion();

        promocion.setComercio(comercio);
        promocion.setTitulo(dto.getTitulo().trim());
        promocion.setDescripcion(dto.getDescripcion());
        promocion.setFechaInicio(dto.getFechaInicio());
        promocion.setFechaFin(dto.getFechaFin());

        Promocion guardada =
                promocionRepository.save(promocion);

        return new PromocionRespuestaDTO(
                guardada.getId(),
                comercio.getId(),
                guardada.getTitulo(),
                guardada.getDescripcion(),
                guardada.getFechaInicio(),
                guardada.getFechaFin()
        );
    }

    private Suscripcion suscripcionActiva(
            Long comercioId) {

        List<Suscripcion> suscripciones =
                suscripcionRepository
                        .findByComercioIdOrderByFechaFinDesc(
                                comercioId
                        );

        for (Suscripcion suscripcion : suscripciones) {

            if (suscripcion.estaActiva()) {
                return suscripcion;
            }
        }

        return null;
    }

    private SuscripcionRespuestaDTO aDTO(
            Suscripcion suscripcion) {

        SuscripcionRespuestaDTO dto =
                new SuscripcionRespuestaDTO();

        dto.setId(suscripcion.getId());
        dto.setComercioId(
                suscripcion.getComercio().getId()
        );
        dto.setPlan(suscripcion.getPlan());
        dto.setMonto(suscripcion.getMonto());
        dto.setFechaInicio(
                suscripcion.getFechaInicio()
        );
        dto.setFechaFin(
                suscripcion.getFechaFin()
        );
        dto.setActiva(
                suscripcion.estaActiva()
        );

        return dto;
    }
}
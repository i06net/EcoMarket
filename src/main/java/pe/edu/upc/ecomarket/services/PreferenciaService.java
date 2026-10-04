package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.dto.PreferenciasDTO;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.Preferencia;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.PreferenciaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenciaService {

    private final PreferenciaRepository preferenciaRepository;
    private final EcoEtiquetaService ecoEtiquetaService;
    private final UsuarioActual usuarioActual;

    public List<EcoEtiquetaDTO> listar() {

        return ecoEtiquetasDe(usuarioActual.obtener().getId())
                .stream()
                .sorted(Comparator.comparing(EcoEtiqueta::getNombre))
                .map(ecoEtiquetaService::aDTO)
                .toList();
    }

    @Transactional
    public List<EcoEtiquetaDTO> guardar(PreferenciasDTO dto) {

        Usuario usuario = usuarioActual.obtener();

        preferenciaRepository.deleteByUsuarioId(usuario.getId());
        preferenciaRepository.flush();

        for (Long id : dto.getEcoEtiquetaIds()) {

            EcoEtiqueta etiqueta =
                    ecoEtiquetaService.buscar(id);

            if (!etiqueta.isActiva()) {
                throw new ReglaNegocioException(
                        "La Eco-Etiqueta " +
                        etiqueta.getNombre() +
                        " está desactivada"
                );
            }

            Preferencia preferencia = new Preferencia();
            preferencia.setUsuario(usuario);
            preferencia.setEcoEtiqueta(etiqueta);

            preferenciaRepository.save(preferencia);
        }

        return listar();
    }

    public Set<EcoEtiqueta> ecoEtiquetasDe(Long usuarioId) {

        return preferenciaRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .map(Preferencia::getEcoEtiqueta)
                .collect(Collectors.toSet());
    }
}
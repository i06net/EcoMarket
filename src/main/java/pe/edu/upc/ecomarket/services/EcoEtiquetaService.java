package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.EcoEtiquetaDTO;
import pe.edu.upc.ecomarket.exceptions.ConflictoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.repository.EcoEtiquetaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoEtiquetaService {

    private final EcoEtiquetaRepository ecoEtiquetaRepository;
    private final ProductoRepository productoRepository;

    public List<EcoEtiquetaDTO> listar(boolean incluirInactivas) {

        List<EcoEtiqueta> etiquetas;

        if (incluirInactivas) {
            etiquetas = ecoEtiquetaRepository.findAllByOrderByNombreAsc();
        } else {
            etiquetas = ecoEtiquetaRepository.findByActivaTrueOrderByNombreAsc();
        }

        return etiquetas.stream()
                .map(this::aDTO)
                .toList();
    }

    public EcoEtiquetaDTO obtener(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional
    public EcoEtiquetaDTO crear(EcoEtiquetaDTO dto) {

        String nombre = dto.getNombre().trim();

        if (ecoEtiquetaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException(
                    "Ya existe una Eco-Etiqueta llamada " + nombre
            );
        }

        EcoEtiqueta etiqueta = new EcoEtiqueta();

        etiqueta.setNombre(nombre);
        etiqueta.setDescripcion(dto.getDescripcion());
        etiqueta.setCriterio(dto.getCriterio());
        etiqueta.setPalabrasClave(dto.getPalabrasClave());

        return aDTO(ecoEtiquetaRepository.save(etiqueta));
    }

    @Transactional
    public EcoEtiquetaDTO actualizar(
            Long id,
            EcoEtiquetaDTO dto) {

        EcoEtiqueta etiqueta = buscar(id);
        String nombre = dto.getNombre().trim();

        if (ecoEtiquetaRepository
                .existsByNombreIgnoreCaseAndIdNot(nombre, id)) {

            throw new ConflictoException(
                    "Ya existe una Eco-Etiqueta llamada " + nombre
            );
        }

        etiqueta.setNombre(nombre);
        etiqueta.setDescripcion(dto.getDescripcion());
        etiqueta.setCriterio(dto.getCriterio());
        etiqueta.setPalabrasClave(dto.getPalabrasClave());
        etiqueta.setActiva(true);

        return aDTO(ecoEtiquetaRepository.save(etiqueta));
    }

    @Transactional
    public boolean eliminar(Long id) {

        EcoEtiqueta etiqueta = buscar(id);

        if (productoRepository.existsByEcoEtiquetasId(id)) {
            etiqueta.setActiva(false);
            ecoEtiquetaRepository.save(etiqueta);
            return true;
        }

        ecoEtiquetaRepository.delete(etiqueta);
        return false;
    }

    public EcoEtiqueta buscar(Long id) {

        return ecoEtiquetaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Eco-Etiqueta",
                                id
                        ));
    }

    public EcoEtiquetaDTO aDTO(EcoEtiqueta etiqueta) {

        return new EcoEtiquetaDTO(
                etiqueta.getId(),
                etiqueta.getNombre(),
                etiqueta.getDescripcion(),
                etiqueta.getCriterio(),
                etiqueta.getPalabrasClave(),
                etiqueta.isActiva()
        );
    }
}
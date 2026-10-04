package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.CategoriaDTO;
import pe.edu.upc.ecomarket.exceptions.ConflictoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.models.Categoria;
import pe.edu.upc.ecomarket.repository.CategoriaRepository;
import pe.edu.upc.ecomarket.repository.ProductoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public CategoriaDTO obtener(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaDTO dto) {

        String nombre = dto.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException(
                    "Ya existe una categoría llamada " + nombre);
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());

        return aDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {

        Categoria categoria = buscar(id);
        String nombre = dto.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException(
                    "Ya existe una categoría llamada " + nombre);
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());

        return aDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = buscar(id);

        if (productoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException(
                    "No se puede eliminar una categoría que tiene productos");
        }

        categoriaRepository.delete(categoria);
    }

    public Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Categoría", id));
    }

    private CategoriaDTO aDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.PerfilDTO;
import pe.edu.upc.ecomarket.dto.UsuarioDTO;
import pe.edu.upc.ecomarket.services.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<UsuarioDTO> listar() {

        return usuarioService.listar();
    }

    @GetMapping("/perfil")
    public UsuarioDTO miPerfil() {

        return usuarioService.miPerfil();
    }

    @PutMapping("/perfil")
    public UsuarioDTO actualizarPerfil(
            @Valid @RequestBody PerfilDTO dto) {

        return usuarioService.actualizarPerfil(dto);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/desactivar")
    public UsuarioDTO desactivar(
            @PathVariable Long id) {

        return usuarioService.desactivar(id);
    }
}
package pe.edu.upc.ecomarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.upc.ecomarket.dto.LoginDTO;
import pe.edu.upc.ecomarket.services.AuthService;
import pe.edu.upc.ecomarket.services.CategoriaService;
import pe.edu.upc.ecomarket.services.ProductoService;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ProductoService productoService;

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("login")) {
            model.addAttribute("login", new LoginDTO());
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @ModelAttribute("login") LoginDTO login,
            Model model) {

        try {
            model.addAttribute("auth", authService.login(login));
            model.addAttribute("correo", login.getCorreo());
            return "login-success";
        } catch (AuthenticationException exception) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String categoria,
            Model model) {

        model.addAttribute("productos", productoService.buscarOrdenado(
                texto,
                categoria,
                null
        ));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("texto", texto == null ? "" : texto);
        model.addAttribute("categoriaSeleccionada", categoria == null ? "" : categoria);
        return "list";
    }
}
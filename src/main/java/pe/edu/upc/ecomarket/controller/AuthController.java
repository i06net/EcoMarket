package pe.edu.upc.ecomarket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.dto.AuthRespuestaDTO;
import pe.edu.upc.ecomarket.dto.LoginDTO;
import pe.edu.upc.ecomarket.dto.RegistroDTO;
import pe.edu.upc.ecomarket.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRespuestaDTO registrar(
            @Valid @RequestBody RegistroDTO dto) {

        return authService.registrar(dto);
    }

    @PostMapping("/login")
    public AuthRespuestaDTO login(
            @Valid @RequestBody LoginDTO dto) {

        return authService.login(dto);
    }
}
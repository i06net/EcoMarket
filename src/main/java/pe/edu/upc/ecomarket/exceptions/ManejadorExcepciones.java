package pe.edu.upc.ecomarket.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.upc.ecomarket.dto.ErrorDTO;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> validacion(MethodArgumentNotValidException ex,
                                                HttpServletRequest request) {

        String mensaje = "Hay campos inválidos";

        return responder(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request
        );
    }

    @ExceptionHandler({ReglaNegocioException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorDTO> reglaNegocio(RuntimeException ex,
                                                  HttpServletRequest request) {

        return responder(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTO> credencialesIncorrectas(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos",
                request
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDTO> usuarioDeshabilitado(
            DisabledException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.UNAUTHORIZED,
                "El usuario está deshabilitado",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> sinSesion(
            AuthenticationException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesión",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> sinPermiso(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta acción",
                request
        );
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorDTO> accesoDenegado(
            AccesoDenegadoException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDTO> noEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorDTO> conflicto(
            ConflictoException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDTO> errorBaseDatos(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.CONFLICT,
                "El dato ya existe o está relacionado con otro registro",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> errorGeneral(
            Exception ex,
            HttpServletRequest request) {

        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado",
                request
        );
    }

    private ResponseEntity<ErrorDTO> responder(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request) {

        ErrorDTO error = new ErrorDTO(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(estado)
                .body(error);
    }
}